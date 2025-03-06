package be.vibes.solver;

import be.vibes.fexpression.DimacsModel;
import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.solver.exception.SolverInitializationException;
import be.vibes.solver.io.uvl.UVLFexprListener;
import de.vill.exception.ParseError;
import de.vill.exception.ParseErrorList;
import de.vill.main.UVLModelFactory;
import de.vill.model.Feature;
import de.vill.model.Group;
import de.vill.model.Import;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.expression.Expression;
import de.vill.util.Constants;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import uvl.UVLJavaLexer;
import uvl.UVLJavaParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class UVLFeatureModelFactory extends UVLModelFactory {

    private final SolverType solverType;
    private final List<ParseError> errorList = new LinkedList<>();

    public UVLFeatureModelFactory() {
        this(SolverType.SAT4J);
    }

    public UVLFeatureModelFactory(SolverType type) {
        super();
        switch (type) {
            case SAT4J -> this.solverType = SolverType.SAT4J;
            case BDD -> this.solverType = SolverType.BDD;
            default -> throw new UnsupportedOperationException("Only SAT4J and BDD solvers are currently supported. Default is SAT4J.");
        }
    }

    @Override
    public FeatureModel parse(String text, Function<String, String> fileLoader) throws ParseError {
        FeatureModel featureModel = parseFeatureModelWithImports(text, fileLoader, new HashMap<>());
        composeFeatureModelFromImports(featureModel);
        referenceFeaturesInConstraints(featureModel);
        validateTypeLevelConstraints(featureModel);
        return featureModel;
    }

    @Override
    public Constraint parseConstraint(String constraintString) throws ParseError {
        constraintString = constraintString.trim();
        UVLJavaLexer UVLJavaLexer = new UVLJavaLexer(CharStreams.fromString(constraintString));
        CommonTokenStream tokens = new CommonTokenStream(UVLJavaLexer);
        UVLJavaParser UVLJavaParser = new UVLJavaParser(tokens);
        UVLJavaParser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        UVLJavaLexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

        UVLJavaLexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                errorList.add(new ParseError(line, charPositionInLine, "failed to parse at line " + line + ":" + charPositionInLine + " due to: " + msg, e));
            }
        });
        UVLJavaParser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                errorList.add(new ParseError(line, charPositionInLine, "failed to parse at line " + line + ":" + charPositionInLine + " due to " + msg, e));
            }
        });

        UVLFexprListener uvlListener = new UVLFexprListener();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(uvlListener, UVLJavaParser.constraintLine());

        return uvlListener.getConstraint();
    }

    private FeatureModel parseFeatureModelWithImports(String text, Function<String, String> fileLoader, Map<String, Import> visitedImports) {
        //remove leading and trailing spaces (to be more robust)
        text = text.trim();
        UVLJavaLexer UVLJavaLexer = new UVLJavaLexer(CharStreams.fromString(text));
        CommonTokenStream tokens = new CommonTokenStream(UVLJavaLexer);
        UVLJavaParser UVLJavaParser = new UVLJavaParser(tokens);
        UVLJavaParser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        UVLJavaLexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

        UVLJavaLexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                errorList.add(new ParseError(line, charPositionInLine, "failed to parse at line " + line + ":" + charPositionInLine + " due to: " + msg, e));
                //throw new ParseError(line, charPositionInLine,"failed to parse at line " + line + ":" + charPositionInLine + " due to: " + msg, e);
            }
        });
        UVLJavaParser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                errorList.add(new ParseError(line, charPositionInLine, "failed to parse at line " + line + ":" + charPositionInLine + " due to " + msg, e));
                //throw new ParseError(line, charPositionInLine,"failed to parse at line " + line + ":" + charPositionInLine + " due to " + msg, e);
            }
        });


        UVLFexprListener uvlListener = new UVLFexprListener();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(uvlListener, UVLJavaParser.featureModel());
        de.vill.model.FeatureModel uvlFM = null;
        FExpression featureDiagram = null;

        try {
            uvlFM = uvlListener.getFeatureModel();
            featureDiagram = uvlListener.getFexpr();
        } catch (ParseErrorList e) {
            errorList.addAll(e.getErrorList());
        }

        if (!errorList.isEmpty()) {
            ParseErrorList parseErrorList = new ParseErrorList("Multiple Errors occurred during parsing!");
            parseErrorList.getErrorList().addAll(errorList);
            throw parseErrorList;
        }

        assert uvlFM != null;

        SolverFacade solver = getSolverFacade(featureDiagram);
        FeatureModel featureModel = new FeatureModel(uvlFM, solver);

        //if featuremodel has not namespace and no root feature getNamespace returns null
        if(featureModel.getNamespace() != null) {
            visitedImports.put(featureModel.getNamespace(), null);

            for (Import importLine : featureModel.getImports()) {
                if (visitedImports.containsKey(importLine.getNamespace()) && visitedImports.get(importLine.getNamespace()) == null) {
                    throw new ParseError("Cyclic import detected! " + "The import of " + importLine.getNamespace() + " in " + featureModel.getNamespace() + " creates a cycle", importLine.getLineNumber());
                } else {
                    try {
                        String path = fileLoader.apply(importLine.getNamespace());
                        Path filePath = Paths.get(path);
                        String content = new String(Files.readAllBytes(filePath));
                        FeatureModel subModel = parseFeatureModelWithImports(content, fileLoader, visitedImports);
                        importLine.setFeatureModel(subModel);
                        subModel.getRootFeature().setRelatedImport(importLine);
                        visitedImports.put(importLine.getNamespace(), importLine);

                        //adjust namespaces of imported features
                        for (Map.Entry<String, Feature> entry : subModel.getFeatureMap().entrySet()) {
                            Feature feature = entry.getValue();
                            if (feature.getNameSpace().isEmpty()) {
                                feature.setNameSpace(importLine.getAlias());
                            } else {
                                feature.setNameSpace(importLine.getAlias() + "." + feature.getNameSpace());
                            }
                        }

                        //check if submodel is actually used
                        if (featureModel.getFeatureMap().containsKey(subModel.getRootFeature().getReferenceFromSpecificSubmodel(""))) {
                            importLine.setReferenced(true);
                        }
                        // if submodel is used add features
                        if (importLine.isReferenced()) {
                            for (Map.Entry<String, Feature> entry : subModel.getFeatureMap().entrySet()) {
                                Feature feature = entry.getValue();
                                if (!featureModel.getFeatureMap().containsKey(feature.getNameSpace() + "." + entry.getValue().getFeatureName())) {
                                    if (importLine.isReferenced()) {
                                        featureModel.getFeatureMap().put(feature.getNameSpace() + "." + entry.getValue().getFeatureName(), feature);
                                    }
                                }
                            }
                        }


                    } catch (IOException e) {
                        throw new ParseError("Could not resolve import: " + e.getMessage(), importLine.getLineNumber());
                    }
                }
            }
        }

        return featureModel;
    }

    private SolverFacade getSolverFacade(FExpression featureDiagram) {

        DimacsModel model;
        try {
            model = DimacsModel.createFromFeatureList(featureDiagram);
        } catch (DimacsFormatException e) {
            throw new ParseError("Unable to initialise the DimacsModel.");
        }

        SolverFacade solver;

        switch (this.solverType) {
            case SAT4J -> {
                try {
                    solver = new Sat4JSolverFacade(model);
                } catch (SolverInitializationException e) {
                    throw new ParseError("Unable to initialise the SAT4J solver.");
                }
            }
            case BDD -> solver = new BDDSolverFacade(featureDiagram);
            default -> throw new UnsupportedOperationException("Only SAT4J and BDD solvers are currently supported. Default is SAT4J.");
        }
        return solver;
    }

    private void composeFeatureModelFromImports(FeatureModel featureModel) {
        for (Map.Entry<String, Feature> entry : featureModel.getFeatureMap().entrySet()) {
            if (entry.getValue().isSubmodelRoot()) {
                Feature featureInMainFeatureTree = entry.getValue();
                Import relatedImport = featureInMainFeatureTree.getRelatedImport();
                Feature featureInSubmodelFeatureTree = relatedImport.getFeatureModel().getRootFeature();
                featureInMainFeatureTree.getChildren().addAll(featureInSubmodelFeatureTree.getChildren());
                for (Group group : featureInMainFeatureTree.getChildren()) {
                    group.setParentFeature(featureInMainFeatureTree);
                }
                featureInMainFeatureTree.getAttributes().putAll(featureInSubmodelFeatureTree.getAttributes());
                relatedImport.getFeatureModel().setRootFeature(featureInMainFeatureTree);
            }
        }
    }

    private List<FeatureModel> createSubModelList(FeatureModel featureModel) {
        List<FeatureModel> subModelList = new LinkedList<>();
        for (Import importLine : featureModel.getImports()) {
            subModelList.add((FeatureModel) importLine.getFeatureModel());
            subModelList.addAll(createSubModelList((FeatureModel) importLine.getFeatureModel()));
        }
        return subModelList;
    }

    private void referenceFeaturesInConstraints(FeatureModel featureModel) {
        List<FeatureModel> subModelList = createSubModelList(featureModel);
        List<LiteralConstraint> literalConstraints = featureModel.getLiteralConstraints();
        for (LiteralConstraint constraint : literalConstraints) {
            Feature referencedFeature = featureModel.getFeatureMap().get(constraint.getLiteral().replace("\'", ""));
            if (referencedFeature == null) {
                throw new ParseError("Feature " + constraint + " is referenced in a constraint in " + featureModel.getNamespace() + " but does not exist as feature in the tree!", constraint.getLineNumber());
            } else {
                constraint.setFeature(referencedFeature);
            }
        }
        for (FeatureModel subModel : subModelList) {
            literalConstraints = subModel.getLiteralConstraints();
            for (LiteralConstraint constraint : literalConstraints) {
                Feature referencedFeature = subModel.getFeatureMap().get(constraint.getLiteral().replace("\'", ""));
                if (referencedFeature == null) {
                    throw new ParseError("Feature " + constraint + " is referenced in a constraint in " + subModel.getNamespace() + " but does not exist as feature in the tree!", constraint.getLineNumber());
                } else {
                    constraint.setFeature(referencedFeature);
                }
            }
        }
    }

    private void validateTypeLevelConstraints(final FeatureModel featureModel) {
        final List<Constraint> constraints = featureModel.getOwnConstraints();
        for (final Constraint constraint: constraints) {
            if (!validateTypeLevelConstraint(constraint)) {
                throw new ParseError("Invalid Constraint in line - " + constraint.getLineNumber());
            }
        }
    }

    private boolean validateTypeLevelConstraint(final Constraint constraint) {
        boolean result = true;
        if (constraint instanceof ExpressionConstraint) {
            String leftReturnType = ((ExpressionConstraint) constraint).getLeft().getReturnType();
            String rightReturnType = ((ExpressionConstraint) constraint).getRight().getReturnType();

            if (!(leftReturnType.equalsIgnoreCase(Constants.TRUE) || rightReturnType.equalsIgnoreCase(Constants.TRUE))) {
                // if not attribute constraint
                result = result && ((ExpressionConstraint) constraint).getLeft().getReturnType().equalsIgnoreCase(((ExpressionConstraint) constraint).getRight().getReturnType());
            }
            if (!result) {
                return false;
            }
            for (final Expression expr: ((ExpressionConstraint) constraint).getExpressionSubParts()) {
                result = result && validateTypeLevelExpression(expr);
            }
        }

        for (final Constraint subCons: constraint.getConstraintSubParts()) {
            result = result && validateTypeLevelConstraint(subCons);
        }

        return result;
    }

    private boolean validateTypeLevelExpression(final Expression expression) {
        final String initial = expression.getReturnType();
        boolean result = true;

        for (final Expression expr: expression.getExpressionSubParts()) {
            result = result && validateTypeLevelExpression(expr) && initial.equalsIgnoreCase(expr.getReturnType());
        }

        return result;
    }
}

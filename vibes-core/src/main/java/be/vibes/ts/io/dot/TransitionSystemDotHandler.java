package be.vibes.ts.io.dot;

import be.vibes.ts.TransitionSystem;
import be.vibes.ts.TransitionSystemFactory;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransitionSystemDotHandler extends DOTBaseListener {

    private TransitionSystemFactory tsFactory;

    private TransitionSystemDotHandler() {}

    @Override
    public void enterNode_stmt(DOTParser.Node_stmtContext ctx) {
        String nodeName = ctx.getChild(0).getText();
        if (tsFactory == null){
            tsFactory = new TransitionSystemFactory(nodeName);
        }
        tsFactory.addState(nodeName);
    }

    @Override
    public void enterEdge_stmt(DOTParser.Edge_stmtContext ctx) {
        String sourceName = ctx.getChild(0).getText(); // Get source node

        // Fallback if sourceName hasn't been defined yet
        if (tsFactory == null) {
            tsFactory = new TransitionSystemFactory(sourceName);
            tsFactory.addState(sourceName);
        }

        // Iterate over all EdgeRHS contexts
        List<DOTParser.EdgeRHSContext> edgeRHSList = ctx.getRuleContexts(DOTParser.EdgeRHSContext.class);
        for (DOTParser.EdgeRHSContext edgeRHS : edgeRHSList) {
            // Iterate over all target nodes in this EdgeRHS
            List<DOTParser.Node_idContext> nodeIds = edgeRHS.getRuleContexts(DOTParser.Node_idContext.class);
            for (DOTParser.Node_idContext nodeCtx : nodeIds) {
                String targetName = nodeCtx.getText();

                String raw = (ctx.attr_list() != null) ? ctx.attr_list().getText() : null;
                String actionName = "τ";
                if (raw != null) {
                    Matcher m = Pattern.compile("label\\s*=\\s*\"([^\"]*)\"").matcher(raw);
                    if (m.find()) {
                        String extracted = m.group(1).trim();
                        if (!extracted.isEmpty()) {
                            actionName = extracted;
                        }
                    }
                }
                tsFactory.addTransition(sourceName, actionName, targetName);
            }
        }
    }

    public static TransitionSystem parseDotFile(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        CharStream input = CharStreams.fromStream(fis);
        DOTLexer lexer = new DOTLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DOTParser parser = new DOTParser(tokens);
        ParseTree tree = parser.graph();
        TransitionSystemDotHandler listener = new TransitionSystemDotHandler();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, tree);
        return listener.tsFactory.build();
    }
}

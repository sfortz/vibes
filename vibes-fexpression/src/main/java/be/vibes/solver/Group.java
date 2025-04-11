package be.vibes.solver;

import be.vibes.fexpression.Feature;
import com.google.common.base.Objects;
import de.vill.config.Configuration;
import de.vill.util.Util;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class represents all kinds of groups (or, alternative, mandatory,
 * optional, cardinality)
 */
public class Group<F extends Feature<F>> {
    /**
     * An enum with all possible group types.
     */
    public enum GroupType {
        OR, ALTERNATIVE, MANDATORY, OPTIONAL, GROUP_CARDINALITY
    }

    /// The type of the group (if type is GROUP_CARDINALITY or FEATURE_CARDINALITY
    /// lower and upper bound must be set!)
    public GroupType GROUPTYPE;
    private final List<F> features;
    private String lowerBound;
    private String upperBound;
    private F parent;

    /**
     * The constructor of the group class.
     *
     * @param groupType The type of the group.
     */
    public Group(GroupType groupType) {
        this.GROUPTYPE = groupType;
        features = new LinkedList<>() {
            /**
             *
             */
            @Serial
            private static final long serialVersionUID = 3856024708694486586L;

            @Override
            public boolean add(F e) {
                if (super.add(e)) {
                    e.setParentGroup(Group.this);
                    return true;
                }
                return false;
            }

            @Override
            public void add(int index, F element) {
                super.set(index, element);
                element.setParentGroup(Group.this);
            }

            @Override
            public F remove(int index) {
                F f = super.remove(index);
                f.setParentGroup(null);
                return f;
            }

            @Override
            public boolean remove(Object o) {
                if (super.remove(o)) {
                    ((F) o).setParentGroup(null);
                    return true;
                }
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends F> c) {
                if (super.addAll(index, c)) {
                    c.forEach(e -> e.setParentGroup(Group.this));
                    return true;
                }
                return false;
            }

            @Override
            public void clear() {
                for (F f : this) {
                    f.setParentGroup(null);
                }
                super.clear();
            }

            @Override
            public F set(int index, F element) {
                F f;
                if ((f = super.set(index, element)) != null) {
                    f.setParentGroup(Group.this);
                    return f;
                }
                return null;
            }

            class FeatureIterator implements ListIterator<F> {
                private final ListIterator<F> itr;
                F lastReturned;

                public FeatureIterator(ListIterator<F> itr) {
                    this.itr = itr;
                }

                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public F next() {
                    lastReturned = itr.next();
                    return lastReturned;
                }

                @Override
                public boolean hasPrevious() {
                    return itr.hasPrevious();
                }

                @Override
                public F previous() {
                    lastReturned = itr.previous();
                    return lastReturned;
                }

                @Override
                public int nextIndex() {
                    return itr.nextIndex();
                }

                @Override
                public int previousIndex() {
                    return itr.previousIndex();
                }

                @Override
                public void remove() {
                    itr.remove();
                    lastReturned.setParentGroup(null);
                }

                @Override
                public void set(F e) {
                    itr.set(e);
                    lastReturned.setParentGroup(null);
                    e.setParentGroup(Group.this);
                }

                @Override
                public void add(F e) {
                    itr.add(e);
                    e.setParentGroup(Group.this);
                }

            }

            @Override
            public ListIterator<F> listIterator(int index) {
                return new FeatureIterator(super.listIterator(index));
            }

            ;
        };
    }

    /**
     * This method only returns a value if the group is a cardinality group. If not,
     * this method returns null.
     *
     * @return null or the lower bound of the group cardinality as string
     */
    public String getLowerBound() {
        return lowerBound;
    }

    /**
     * Set the lower bound (only if the group is a cardinality group).
     *
     * @param lowerBound the lower bound of the group
     */
    public void setLowerBound(String lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * This method only returns a value if the group is a cardinality group. If not,
     * this method returns null. If there is no upper bound in the cardinality, this
     * method returns the lower bound. The returned value might also be the *
     * symbol, if the upper bound is unlimited.
     *
     * @return the upper bound as string
     */
    public String getUpperBound() {
        return upperBound;
    }

    /**
     * Set the upper bound of the group (only if the group is a cardinality group).
     *
     * @param upperBound the upper bound of the group (may be * symbol)
     */
    public void setUpperBound(String upperBound) {
        this.upperBound = upperBound;
    }

    /**
     * Returns all children of the group (Features). If there are non an empty list
     * is returned, not null. The returned list is not a copy, which means editing
     * this list will actually change the group.
     *
     * @return A list with all child features.
     */
    public List<F> getFeatures() {
        return features;
    }

    /**
     * Returns the group and all its children as uvl valid string.
     *
     * @param withSubmodels true if the featuremodel is printed as composed
     *                      featuremodel with all its submodels as one model, false
     *                      if the model is printed with separated sub models
     * @param currentAlias  the namspace from the one referencing the group (or the
     *                      features in the group) to the group (or the features in
     *                      the group)
     * @return uvl representaiton of the group
     */
    public String toString(boolean withSubmodels, String currentAlias) {
        StringBuilder result = new StringBuilder();

        switch (GROUPTYPE) {
            case OR:
                result.append("or");
                break;
            case ALTERNATIVE:
                result.append("alternative");
                break;
            case OPTIONAL:
                result.append("optional");
                break;
            case MANDATORY:
                result.append("mandatory");
                break;
            case GROUP_CARDINALITY:
                result.append(getCardinalityAsSting());
                break;
        }

        result.append(Configuration.getNewlineSymbol());

        for (F feature : features) {
            result.append(Util.indentEachLine(feature.toString(withSubmodels, currentAlias)));
        }

        return result.toString();
    }

    private String getCardinalityAsSting() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        if (getLowerBound().equals(getUpperBound())) {
            result.append(getLowerBound());
        } else {
            result.append(getLowerBound());
            result.append("..");
            result.append(getUpperBound());
        }
        result.append("]");
        return result.toString();
    }

    @Override
    public Group<F> clone() {
        Group<F> group = new Group<>(GROUPTYPE);
        group.setUpperBound(getUpperBound());
        group.setLowerBound(getLowerBound());
        for (F feature : getFeatures()) {
            group.getFeatures().add((F) feature.clone());
        }
        for (F feature : group.getFeatures()) {
            feature.setParentGroup(group);
        }
        return group;
    }

    /**
     * Returns the parent feature of the group.
     *
     * @return Parent Feature of the group.
     */
    public F getParentFeature() {
        return parent;
    }

    /**
     * Sets the parent feature of the group.
     *
     * @param parent The parent feature of the group.
     */
    public void setParentFeature(F parent) {
        this.parent = parent;
    }

    /*
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Group)) {
            return false;
        }

        if (this.GROUPTYPE != ((Group<F>) obj).GROUPTYPE) {
            return false;
        }

        if (this.getUpperBound() != null && !(this.getUpperBound().equals(((Group<F>) obj).getUpperBound()))) {
            return false;
        }

        if (this.getLowerBound() != null && !(this.getLowerBound().equals(((Group<F>) obj).getLowerBound()))) {
            return false;
        }

        if (this.getFeatures().size() != ((Group<F>) obj).getFeatures().size()) {
            return false;
        }

        final List<F> objFeatures = ((Group<F>) obj).getFeatures();
        for (final F currentFeature : this.getFeatures()) {
            if (!objFeatures.contains(currentFeature)) {
                return false;
            }
        }

        return true;
    }*/

    /*
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Group<?> group = (Group<?>) o;
        return GROUPTYPE == group.GROUPTYPE && Objects.equal(getFeatures(), group.getFeatures()) && Objects.equal(parent, group.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(GROUPTYPE, getFeatures(), getLowerBound(), getUpperBound(), parent);
    }*/

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Group<?> group = (Group<?>) o;
        return GROUPTYPE == group.GROUPTYPE && Objects.equal(getFeatures(), group.getFeatures());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(GROUPTYPE, getFeatures());
    }
}

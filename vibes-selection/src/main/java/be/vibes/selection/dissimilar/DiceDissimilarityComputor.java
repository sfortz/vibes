
package be.vibes.selection.dissimilar;

import java.util.Set;

/**
 *
 * @author Xavier Devroey - xavier.devroey@unamur.be
 * @param <T>
 */
public class DiceDissimilarityComputor <T extends Set> extends JaccardDissimilarityComputor<T> {
    
    public DiceDissimilarityComputor(){
        super(0.5);
    }

}

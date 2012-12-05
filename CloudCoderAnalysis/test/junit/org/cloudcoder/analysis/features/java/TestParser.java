package junit.org.cloudcoder.analysis.features.java;

import org.cloudcoder.analysis.features.java.FeatureVisitor;
import org.junit.Test;

public class TestParser
{
    @Test
    public void testFeatureExtract() throws Exception
    {
        String filePath="files/parser/MissingSemicolon.java";
        FeatureVisitor visitor=new FeatureVisitor();
        visitor.extractFeatures(filePath);
    }
}

package org.wso2.carbon.connector.as400.pcml;

/**
 * Created by pta75590 on 8/11/2016.
 */
public class PCMLInputParam {
    private String qualifiedName;
    private int[] indices;
    private String value;

    public PCMLInputParam(String qualifiedName, int[] indices, String value) {
        this.qualifiedName = qualifiedName;
        this.indices = indices;
        this.value = value;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

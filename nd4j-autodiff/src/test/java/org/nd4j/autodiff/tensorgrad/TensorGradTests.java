package org.nd4j.autodiff.tensorgrad;

import org.junit.Test;
import org.nd4j.autodiff.opstate.OpExecAction;
import org.nd4j.autodiff.opstate.OpState;
import org.nd4j.autodiff.tensorgrad.impl.TensorGradVariable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.Op;
import org.nd4j.linalg.api.ops.impl.transforms.Sigmoid;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.util.ArrayUtil;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by agibsonccc on 4/11/17.
 */
public class TensorGradTests {
    @Test
    public void testSigmoid() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Nd4j.linspace(1,4,4);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable sigmoid = tensorGrad.sigmoid(x);
        assertEquals("sigmoid(x)",sigmoid.getVarName());
        assertEquals(2,tensorGrad.graph().numVertices());
        assertEquals(1,tensorGrad.graph().getEdges().size());
        assertArrayEquals(arr.shape(),sigmoid.getShape());
        assertEquals(1,tensorGrad.graph().getVertexInDegree(1));
        assertArrayEquals(new int[]{0,1},tensorGrad.graph().topologicalSort());
        assertEquals(1,tensorGrad.graph().getOpOrder().getActions().size());
        OpState opState = tensorGrad.graph().getOpOrder().getActions().get(0).getOpState();
        assertEquals("sigmoid",opState.getOpName());
        tensorGrad.allocate();
        Op op = tensorGrad.createOp(OpState.OpType.TRANSFORM,tensorGrad.graph().getOpOrder().getActions().get(0));
        assertTrue(op instanceof Sigmoid);
        Nd4j.getExecutioner().exec(op);
        assertEquals(Transforms.sigmoid(Nd4j.linspace(1,4,4)),op.z());

    }

    @Test
    public void testSum() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Transforms.sigmoid(Nd4j.linspace(1,4,4));
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable result = tensorGrad.sum(x,1);
        assertEquals("sum(x)",result.getVarName());
        assertEquals(2,tensorGrad.graph().numVertices());
        assertEquals(1,tensorGrad.graph().getEdges().size());
        assertArrayEquals(arr.shape(),result.getShape());
        assertArrayEquals(new int[]{0,1},tensorGrad.graph().topologicalSort());


    }



    @Test
    public void testReshape() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Transforms.sigmoid(Nd4j.linspace(1,4,4)).reshape(2,2);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable result = tensorGrad.reshape(x);
        assertEquals("reshape(x)",result.getVarName());
        assertEquals(2,tensorGrad.graph().numVertices());
        assertEquals(1,tensorGrad.graph().getEdges().size());
        assertArrayEquals(new int[]{2,2},result.getShape());

    }

    @Test
    public void testTranspose() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Transforms.sigmoid(Nd4j.linspace(1,4,4));
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable result = tensorGrad.transpose(x);
        assertEquals("transpose(x)",result.getVarName());
        assertEquals(2,tensorGrad.graph().numVertices());
        assertEquals(1,tensorGrad.graph().getEdges().size());
        assertArrayEquals(new int[]{4,1},result.getShape());

    }

    @Test
    public void testDistance() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Transforms.sigmoid(Nd4j.linspace(1,4,4)).reshape(2,2);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable y = tensorGrad.var("y",arr);
        TensorGradVariable result = tensorGrad.cosineSimilarity(x,y,1);
        assertEquals("cosineSimilarity(x,y)",result.getVarName());
        assertEquals(3,tensorGrad.graph().numVertices());
        assertEquals(2,tensorGrad.graph().getEdges().size());
        assertArrayEquals(new int[]{1,2},result.getShape());
    }

    @Test
    public void testTensorGradMmul() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Transforms.sigmoid(Nd4j.linspace(1,4,4)).reshape(2,2);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable y = tensorGrad.var("y",arr);
        TensorGradVariable result = tensorGrad.mmul(0,x,y);
        TensorGradVariable otherResult = result.add(result);
        assertEquals("mmul(x,y)",result.getVarName());
        assertEquals(3,tensorGrad.graph().numVertices());
        assertEquals(2,tensorGrad.graph().getEdges().size());
        assertArrayEquals(new int[]{2,2},result.getShape());
    }


    @Test
    public void testGetInputs() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Transforms.sigmoid(Nd4j.linspace(1,4,4)).reshape(2,2);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable y = tensorGrad.var("y",arr);
        TensorGradVariable result = tensorGrad.mmul(0,x,y);
        TensorGradVariable otherResult = result.add(result);
        assertEquals(2,tensorGrad.graph().getInputs().size());
    }

    @Test
    public void testGetOutputs() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Transforms.sigmoid(Nd4j.linspace(1,4,4)).reshape(2,2);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable y = tensorGrad.var("y",arr);
        TensorGradVariable result = tensorGrad.mmul(0,x,y);
        TensorGradVariable otherResult = result.add(result);
        assertEquals(2,tensorGrad.graph().getOutputs().size());
    }

    @Test
    public void testEval() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Nd4j.linspace(1,4,4);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable sigmoid = tensorGrad.sigmoid(x);
        INDArray assertion = Transforms.sigmoid(arr);
        INDArray[] eval = tensorGrad.eval(Collections.singletonMap("x",arr));
        assertEquals(assertion,eval[0]);

    }

    @Test
    public void testEvalAddSelf() {
        /**
         * Note this test fails yet due to needing
         * to validate simple cases like x * x
         * matching number of inputs.
         */
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Nd4j.linspace(1,4,4);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable sigmoid = x.mul(x);
        INDArray assertion = arr.mul(arr);
        INDArray[] eval = tensorGrad.eval(Collections.singletonMap("x",arr));
        assertEquals(assertion,eval[0]);

    }

    @Test
    public void testEvalAdd() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Nd4j.linspace(1,4,4);
        INDArray yArr = arr.dup();
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable y = tensorGrad.var("y",yArr);

        TensorGradVariable sigmoid = x.mul(y);
        INDArray assertion = arr.mul(arr);
        Map<String,INDArray> vars = new HashMap<>();
        vars.put("x",arr);
        vars.put("y",yArr);
        INDArray[] eval = tensorGrad.eval(vars);
        assertEquals(assertion,eval[0]);

    }

    @Test
    public void testTensorGradTensorMmul() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Transforms.sigmoid(Nd4j.linspace(1,8,8)).reshape(2,2,2);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable y = tensorGrad.var("y",arr);
        TensorGradVariable result = tensorGrad.tensorMmul(x,y,new int[][]{{0},{1}},0);
        assertEquals("tensorMmul(x,y)",result.getVarName());
        assertEquals(3,tensorGrad.graph().numVertices());
        assertEquals(2,tensorGrad.graph().getEdges().size());
        assertArrayEquals(ArrayUtil.getTensorMmulShape(new int[]{2,2,2},new int[]{2,2,2},new int[][]{{0},{1}}),result.getShape());
        assertEquals(32,tensorGrad.numElements());
    }

    @Test
    public void testDup() {
        TensorGrad tensorGrad = TensorGrad.create();
        INDArray arr = Transforms.sigmoid(Nd4j.linspace(1,8,8)).reshape(2,2,2);
        TensorGradVariable x = tensorGrad.var("x",arr);
        TensorGradVariable y = tensorGrad.var("y",arr);
        TensorGrad tg2 = tensorGrad.dup();
        assertEquals(tensorGrad,tg2);
    }

    @Test
    public void testOpExecutionWithAutoDiff() {
        TensorGrad tensorGrad = TensorGrad.create();
        tensorGrad.getArrayFactory().toString();
        INDArray arr = Nd4j.linspace(1,4,4);
        TensorGradVariable x = tensorGrad.var("x",arr);
        tensorGrad.toString();
        TensorGradVariable sigmoid = tensorGrad.sigmoid(x);
        TensorGradVariable grad = tensorGrad.grad(x,sigmoid);
        List<OpExecAction> states = tensorGrad.graph().getOpOrder().getActions();
        OpState opState = tensorGrad.graph().getOpOrder().getActions().get(0).getOpState();
        assertEquals("sigmoid",opState.getOpName());
        tensorGrad.allocate();
        Op op = tensorGrad.createOp(OpState.OpType.TRANSFORM,tensorGrad.graph().getOpOrder().getActions().get(0));
        assertTrue(op instanceof Sigmoid);
        Nd4j.getExecutioner().exec(op);
        assertEquals(Transforms.sigmoid(Nd4j.linspace(1,4,4)),op.z());

    }

}

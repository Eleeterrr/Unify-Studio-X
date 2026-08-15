package eleeter.unifystudiox.graphics.math;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TransformStack
{
    private static final int DEFAULT_CAPACITY = 32;

    private final Matrix4f[] stack;
    private int pointer = 0;

    /**
     * Creates a new TransformStack with a default depth of 32.
     */
    public TransformStack()
    {
        this(DEFAULT_CAPACITY);
    }


    public TransformStack(int capacity)
    {
        this.stack = new Matrix4f[capacity];
        for (int i = 0; i < capacity; i++)
        {
            this.stack[i] = new Matrix4f();
        }
    }


    public TransformStack push()
    {
        if (this.pointer + 1 >= this.stack.length)
        {
            throw new StackOverflowError("TransformStack capacity exceeded (" + this.stack.length + ")");
        }
        this.pointer++;
        this.stack[this.pointer].set(this.stack[this.pointer - 1]);
        return this;
    }


    public TransformStack pop()
    {
        if (this.pointer <= 0)
        {
            throw new IllegalStateException("TransformStack underflow (cannot pop the base identity)");
        }
        this.pointer--;
        return this;
    }


    public TransformStack identity()
    {
        this.stack[this.pointer].identity();
        return this;
    }


    public Matrix4f peek()
    {
        return this.stack[this.pointer];
    }

    public Matrix4f last()
    {
        return this.stack[this.pointer];
    }


    public TransformStack translate(float x, float y, float z)
    {
        this.stack[this.pointer].translate(x, y, z);
        return this;
    }

    public TransformStack translate(Vector3f vec)
    {
        this.stack[this.pointer].translate(vec);
        return this;
    }

    public TransformStack rotate(float angle, float x, float y, float z)
    {
        this.stack[this.pointer].rotate(angle, x, y, z);
        return this;
    }

    public TransformStack rotate(Quaternionf quat)
    {
        this.stack[this.pointer].rotate(quat);
        return this;
    }

    public TransformStack rotateX(float angle)
    {
        this.stack[this.pointer].rotateX(angle);
        return this;
    }

    public TransformStack rotateY(float angle)
    {
        this.stack[this.pointer].rotateY(angle);
        return this;
    }

    public TransformStack rotateZ(float angle)
    {
        this.stack[this.pointer].rotateZ(angle);
        return this;
    }

    public TransformStack scale(float s)
    {
        this.stack[this.pointer].scale(s);
        return this;
    }

    public TransformStack scale(float x, float y, float z)
    {
        this.stack[this.pointer].scale(x, y, z);
        return this;
    }

    public TransformStack mul(Matrix4f other)
    {
        this.stack[this.pointer].mul(other);
        return this;
    }


    public TransformStack set(Matrix4f other)
    {
        this.stack[this.pointer].set(other);
        return this;
    }

    public int getDepth()
    {
        return this.pointer;
    }


    public int getCapacity()
    {
        return this.stack.length;
    }
}

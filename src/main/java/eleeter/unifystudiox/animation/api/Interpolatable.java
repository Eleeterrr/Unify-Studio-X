package eleeter.unifystudiox.animation.api;


public interface Interpolatable<T>
{
    T interpolate(T from, T to, float t);
}

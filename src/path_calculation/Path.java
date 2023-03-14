package path_calculation;

import java.util.Objects;

/**
 * Represents a path between nodes.
 * @author Jakub Krizanovsky
 */
public class Path {
	/** Node 1 index. */
	public final int u;
	/** Node 2 index. */
	public final int v;
	
	/**
	 * Constructs a path.
	 * @param u Node 1 index.
	 * @param v Node 2 index.
	 */
	public Path(int u, int v) {
		this.u = u;
		this.v = v;
	}

	/**
	 * Returns a hashcode of the object.
	 * @return Hashcode of the object.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(u, v);
	}

	/**
	 * Checks whether the provided object is equal to the path.
	 * @return True, if provided object is equal to the path, else false.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Path other = (Path) obj;
		return u == other.u && v == other.v;
	}
	
	/**
	 * Returns an inverse path.
	 * @return Inverse path.
	 */
	public Path inverse() {
		return new Path(v, u);
	}
}

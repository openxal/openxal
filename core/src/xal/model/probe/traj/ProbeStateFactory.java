package xal.model.probe.traj;

/**
 * Interface used to create a <code>ProbeState</code> of the correct
 * type <b><code>T</code></b>.
 * 
 * @param <T> the type of ProbeState to create
 * 
 * @author Jonathan M. Freed
 */

public interface ProbeStateFactory<T extends ProbeState> {
	T create();
}

package xal.model.probe.traj;

public interface ProbeStateFactory<T extends ProbeState> {
	T create();
}

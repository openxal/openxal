package xal.sim.scenario;

import xal.model.IComponent;

public abstract class Converter {
	protected boolean thin = false;
	public boolean isThin()
	{
		return thin;
	}
	public abstract IComponent convert(PositionedElement element);
}
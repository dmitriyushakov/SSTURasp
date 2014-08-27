package sstuclient;

public abstract class LazyFaculty extends Faculty{
	protected Faculty realFaculty;
	@Override
	public String getName() {
		return name;
	}
	@Override
	public int size() {
		if(realFaculty==null)return 0;
		else return realFaculty.size();
	}
	@Override
	public SpecialityTag at(int num) {
		if(realFaculty==null)return null;
		else return realFaculty.at(num);
	}
}

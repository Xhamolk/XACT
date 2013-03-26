package xk.xact.client.button;


public interface ICustomButtonMode {

	@Override
	public String toString();

	public static final ICustomButtonMode NULL = new ICustomButtonMode() {
		@Override
		public String toString() {
			return "NULL";
		}
	};


	public static enum ItemModes implements ICustomButtonMode {
		NORMAL, SPECIAL;

		@Override
		public String toString() {
			return this == NORMAL ? "Normal Item" : "Special Item";
		}
	}

	public static enum DeviceModes implements ICustomButtonMode {
		INACTIVE, SAVE, LOAD, CLEAR;

		@Override
		public String toString() {
			return "Device Mode - " + this.name();
		}
	}

}

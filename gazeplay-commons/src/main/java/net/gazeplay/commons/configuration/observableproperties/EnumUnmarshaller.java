package net.gazeplay.commons.configuration.observableproperties;

import java.util.function.Function;

public class EnumUnmarshaller<T extends Enum<T>> implements Function<String, T> {

	private final Class<T> enumClass;

	public EnumUnmarshaller(final Class<T> enumClass) {
		super();
		this.enumClass = enumClass;
	}

	@Override
	public T apply(final String name) {
		return Enum.valueOf(enumClass, name);
	}
}

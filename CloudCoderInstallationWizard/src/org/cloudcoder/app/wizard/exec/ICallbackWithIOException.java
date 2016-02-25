package org.cloudcoder.app.wizard.exec;

import java.io.IOException;

public interface ICallbackWithIOException<E> {
	public void call(E value) throws IOException;
}

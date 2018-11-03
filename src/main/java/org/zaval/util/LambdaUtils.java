/*
 * Copyright (C) 2001-2002  Zaval Creative Engineering Group (http://www.zaval.org)
 * Copyright (C) 2019 Christoph Obexer <cobexer@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * (version 2) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.zaval.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LambdaUtils {
	// from http://4comprehension.com/sneakily-throwing-exceptions-in-lambda-expressions-in-java/
	public static <R> Supplier<R> unchecked(ThrowingSupplier<R> f) { // NO_UCD (use private)
		return () -> {
			try {
				return f.get();
			}
			catch (Exception ex) {
				return sneakyThrow(ex);
			}
		};
	}

	public static <T> Consumer<T> unchecked(ThrowingConsumer<T> f) { // NO_UCD (use private)
		return (T t) -> {
			try {
				f.accept(t);
			}
			catch (Exception ex) {
				sneakyThrow(ex);
			}
		};
	}

	// from http://4comprehension.com/sneakily-throwing-exceptions-in-lambda-expressions-in-java/
	@SuppressWarnings("unchecked")
	private static <T extends Exception, R> R sneakyThrow(Exception t) throws T { // NO_UCD (use private)
		throw (T) t; // ( ͡° ͜ʖ ͡°)
	}

	@FunctionalInterface
	public interface ThrowingSupplier<T> {
		T get() throws Exception;
	}

	@FunctionalInterface
	public interface ThrowingConsumer<T> {
		void accept(T t) throws Exception;
	}
}

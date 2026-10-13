package org.chagolchana.chagol.db;

interface BenchmarkTask<T> {

	void run(T context) throws Exception;
}

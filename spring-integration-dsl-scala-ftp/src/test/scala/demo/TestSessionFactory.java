/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPFile;

import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
/**
 * @author Oleg Zhurakousky
 */
public class TestSessionFactory extends DefaultFtpSessionFactory {

	public TestSessionFactory(){}

	@Override
	public Session<FTPFile> getSession() {
		Session<FTPFile> session = new Session<FTPFile>() {

			public boolean remove(String path) throws IOException {
				return true;
			}

			public FTPFile[] list(String path) throws IOException {
				return new FTPFile[]{};
			}

			public void read(String source, OutputStream outputStream)
					throws IOException {
			}

			public void write(InputStream inputStream, String destination)
					throws IOException {
			}

			public boolean mkdir(String directory) throws IOException {
				return true;
			}

			public void rename(String pathFrom, String pathTo)
					throws IOException {
			}

			public void close() {
			}

			public boolean isOpen() {
				return true;
			}

			public boolean exists(String path) throws IOException {
				return true;
			}

			public String[] listNames(String path) throws IOException {
				return new String[]{};
			}
		};
		return session;
	}

}

/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpSession;

/**
 * @author Oleg Zhurakousky
 */
public class TestSessionFactory extends DefaultFtpSessionFactory {

	public TestSessionFactory() {
	}

	@Override
	public FtpSession getSession() {

		FTPClient ftpClient = new FTPClient();
		
		return new FtpSession(ftpClient) {

			@Override
			public boolean remove(String path) throws IOException {
				return true;
			}

			@Override
			public FTPFile[] list(String path) throws IOException {
				return new FTPFile[] {};
			}

			@Override
			public String[] listNames(String path) throws IOException {
				return new String[]{};
			}

			@Override
			public void read(String path, OutputStream fos) throws IOException {
			}

			@Override
			public InputStream readRaw(String source) throws IOException {
				return super.readRaw(source);
			}

			@Override
			public boolean finalizeRaw() throws IOException {
				return true;
			}

			@Override
			public void write(InputStream inputStream, String path)
					throws IOException {
			}

			@Override
			public void append(InputStream inputStream, String path)
					throws IOException {
			}

			@Override
			public void close() {
			}

			@Override
			public boolean isOpen() {
				return true;
			}

			@Override
			public void rename(String pathFrom, String pathTo)
					throws IOException {
			}

			@Override
			public boolean mkdir(String remoteDirectory) throws IOException {
				return true;
			}

			@Override
			public boolean rmdir(String directory) throws IOException {
				return true;
			}

			@Override
			public boolean exists(String path) throws IOException {
				return true;
			}

			@Override
			public FTPClient getClientInstance() {
				return super.getClientInstance();
			}

		};
	}

}

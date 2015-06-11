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

import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author Oleg Zhurakousky
 */
public class TestSessionFactory extends DefaultSftpSessionFactory {

	public TestSessionFactory() {
	}

	@Override
	public SftpSession getSession() {
		
		   JSch jsch = new JSch();
	       Session session = null;
		try {
			session = jsch.getSession("Bertrand Russell", "localhost", 22);
		} catch (JSchException e) {
			e.printStackTrace();
		}

		return new SftpSession(session) {

			@Override
			public boolean remove(String path) throws IOException {
				return super.remove(path);
			}

			@Override
			public LsEntry[] list(String path) throws IOException {
				return super.list(path);
			}

			@Override
			public String[] listNames(String path) throws IOException {
				return super.listNames(path);
			}

			@Override
			public void read(String source, OutputStream os) throws IOException {
				super.read(source, os);
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
			public void write(InputStream inputStream, String destination)
					throws IOException {
			}

			@Override
			public void append(InputStream inputStream, String destination)
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
			public boolean rmdir(String remoteDirectory) throws IOException {

				return true;
			}

			@Override
			public boolean exists(String path) {
				return true;
			}

			@Override
			public ChannelSftp getClientInstance() {
				return super.getClientInstance();
			}

		};
	}

}

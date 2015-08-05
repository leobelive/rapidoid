package org.rapidoid.test;

/*
 * #%L
 * rapidoid-integration-tests
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski and contributors
 * %%
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
 * #L%
 */

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.ctx.Ctxs;
import org.rapidoid.http.HTTP;
import org.rapidoid.http.HTTPServer;
import org.rapidoid.http.Handler;
import org.rapidoid.http.HttpExchange;
import org.rapidoid.job.Jobs;
import org.rapidoid.log.Log;
import org.rapidoid.webapp.WebApp;
import org.rapidoid.webapp.WebAppGroup;

@Authors("Nikolche Mihajlovski")
@Since("4.1.0")
public class AsyncHttpServerTest extends IntegrationTestCommons {

	@Test
	public void testAsyncHttpServer() {
		Log.debugging();
		HTTP.DEFAULT_CLIENT.reset();
		WebApp app = WebAppGroup.openRootContext();

		app.getRouter().generic(new Handler() {

			@Override
			public Object handle(final HttpExchange x) throws Exception {
				Jobs.schedule(new Runnable() {

					@Override
					public void run() {
						x.write("O");

						Jobs.schedule(new Runnable() {
							@Override
							public void run() {
								x.write("K");
								x.done();
							}
						}, 1, TimeUnit.SECONDS);

					}

				}, 1, TimeUnit.SECONDS);

				return x;
			}

		});

		HTTPServer server = HTTP.server().applications(WebAppGroup.main()).build().start();

		eq(new String(HTTP.get("http://localhost:8080/")), "OK");
		eq(new String(HTTP.post("http://localhost:8080/")), "OK");

		server.shutdown();
		Ctxs.close();
	}

}
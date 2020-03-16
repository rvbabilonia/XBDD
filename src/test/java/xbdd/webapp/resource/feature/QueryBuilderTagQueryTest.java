/*
 * Copyright (C) 2015 Orion Health (Orchestral Development Ltd)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xbdd.webapp.resource.feature;

import static de.flapdoodle.embed.mongo.distribution.Version.Main.V4_0;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.store.HttpProxyFactory;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.MongoImportStarter;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.runtime.Network;

public class QueryBuilderTagQueryTest {

	private MongodExecutable mongodExecutable;
	private MongoClient mongo;
	private final String dbName = "testDB";
	private final String collection = "features";

	@Before
	public void before() throws IOException {
		final int port = 47341;
		final boolean upsert = true;
		final boolean drop = true;
		final boolean jsonArray = true;
		final String jsonFile = "src/test/resources/xbdd/tag-test-report.json";

		final IMongodConfig mongoConfigConfig = new MongodConfigBuilder()
				.version(Versions.withFeatures(V4_0, Version.Main.PRODUCTION.getFeatures()))
				.net(new Net(port, Network.localhostIsIPv6()))
				.configServer(false)
				.build();

		if (StringUtils.isNotBlank(System.getenv("https.proxyHost"))) {
			IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
					.defaults(Command.MongoD)
					.artifactStore(new ExtractedArtifactStoreBuilder()
							.defaults(Command.MongoD)
							.download(new DownloadConfigBuilder()
									.defaultsForCommand(Command.MongoD)
									// e.g. https.proxyHost=proxy.example.com;https.proxyPort=3128
									.proxyFactory(new HttpProxyFactory(System.getenv("https.proxyHost"), Integer.parseInt(System.getenv("https.proxyPort"))))
									.build()))
					.build();

			mongodExecutable = MongodStarter.getInstance(runtimeConfig).prepare(mongoConfigConfig);
		} else {
			mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongoConfigConfig);
		}

		mongodExecutable.start();
		final IMongoImportConfig mongoImportConfig = new MongoImportConfigBuilder()
				.version(Versions.withFeatures(V4_0, Version.Main.PRODUCTION.getFeatures()))
				.net(new Net(port, Network.localhostIsIPv6()))
				.db(dbName)
				.collection(collection)
				.upsert(upsert)
				.dropCollection(drop)
				.jsonArray(jsonArray)
				.importFile(jsonFile)
				.build();

		MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig).start();

		mongo = new MongoClient("localhost", port);
	}

	@After
	public void after() {
		// this will stop mongoImportExecutable as both mongodExecutable and mongoImportExecutable use the same mongod.
		if (mongodExecutable != null) {
			mongodExecutable.stop();
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTagQuery() {
		final MongoDatabase db = mongo.getDatabase(dbName);
		final MongoCollection col = db.getCollection(collection);

		final BasicDBObject query = new BasicDBObject();
		final List<DBObject> tagQuery = QueryBuilder.getInstance().buildHasTagsQuery();
		query.append("$and", tagQuery);

		FindIterable<Document> findIterable = col.find(query, Document.class);
		final MongoCursor<Document> cursor = findIterable.iterator();

		assertThat(col.countDocuments(query)).isEqualTo(3L);

		while (cursor.hasNext()) {
			final Document next = cursor.next();
			List<Document> elements;

			boolean tags = next.containsKey("tags");

			if (next.containsKey("elements")) {
				elements = (List<Document>) next.get("elements");
				for (final Document element : elements) {
					if (element.containsKey("tags"))
					{
						tags = true;
						break;
					}
				}
			}

			assertThat(tags).isTrue();
		}
	}
}

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

import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.List;

import org.bson.Document;
import org.junit.After;
import org.junit.Assert;
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
import de.flapdoodle.embed.mongo.config.IMongoImportConfig;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongoImportConfigBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
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
				.version(Version.Main.PRODUCTION)
				.net(new Net(port, Network.localhostIsIPv6()))
				.configServer(false)
				.build();

		this.mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongoConfigConfig);
		this.mongodExecutable.start();
		final IMongoImportConfig mongoImportConfig = new MongoImportConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(port, Network.localhostIsIPv6()))
				.db(this.dbName)
				.collection(this.collection)
				.upsert(upsert)
				.dropCollection(drop)
				.jsonArray(jsonArray)
				.importFile(jsonFile)
				.build();

		MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig).start();

		this.mongo = new MongoClient("localhost", port);
	}

	@After
	public void after() {
		// this will stop mongoImportExecutable as both mongodExecutable and mongoImportExecutable use the same mongod.
		if (this.mongodExecutable != null) {
			this.mongodExecutable.stop();
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTagQuery() {
		final MongoDatabase db = this.mongo.getDatabase(this.dbName);
		final MongoCollection col = db.getCollection(this.collection);

		final BasicDBObject query = new BasicDBObject();
		final List<DBObject> tagQuery = QueryBuilder.getInstance().buildHasTagsQuery();
		query.append("$and", tagQuery);

		FindIterable<Document> findIterable = col.find(query, Document.class);
		final MongoCursor<Document> cursor = findIterable.iterator();

		Assert.assertThat(col.countDocuments(query), equalTo(3L));

		while (cursor.hasNext()) {
			final Document next = cursor.next();
			List<Document> elements;

			boolean tags = next.containsKey("tags");

			if (next.containsKey("elements")) {
				elements = (List<Document>) next.get("elements");
				for (final Document element : elements) {
					if (element.containsKey("tags")) {
						tags = true;
					}
				}
			}

			Assert.assertTrue(tags);
		}
	}
}
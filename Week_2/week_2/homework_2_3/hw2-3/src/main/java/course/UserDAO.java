/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package course;

import com.mongodb.*;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class UserDAO {
    private final DBCollection usersCollection;
    private Random random = new SecureRandom();

    public UserDAO(final DB blogDatabase) {
        usersCollection = blogDatabase.getCollection("users");
    }

    // validates that username is unique and insert into db
    public boolean addUser(String username, String password, String email) {

        String passwordHash = makePasswordHash(password, Integer.toString(random.nextInt()));

        // XXX WORK HERE
        // create an object suitable for insertion into the user collection
        // be sure to add username and hashed password to the document. problem instructions
        // will tell you the schema that the documents must follow.

        BasicDBObject newUser = new BasicDBObject("_id", username).append("password", passwordHash);

        if (email != null && !email.equals("")) {
            // XXX WORK HERE
            // if there is an email address specified, add it to the document too.
            newUser.append("email",email);
        }

        try {
            // XXX WORK HERE
            // insert the document into the user collection here
            usersCollection.insert(newUser);
            return true;
        } catch (MongoException.DuplicateKey e) {
            System.out.println("Username already in use: " + username);
            return false;
        }
    }

    public DBObject validateLogin(String username, String password) {
        DBObject user = null;

        // XXX look in the user collection for a user that has this username
        // assign the result to the user variable.

        DBCursor cursor =  usersCollection.find(new BasicDBObject("_id", username)).limit(1);


        if (cursor.hasNext()){

            user = cursor.next();
            System.out.println("User:" + user.toString());
        }

        if (user == null) {
            System.out.println("User not in database");
            return null;
        }

        String hashedAndSalted = user.get("password").toString();

        String salt = hashedAndSalted.split(",")[1];

        if (!hashedAndSalted.equals(makePasswordHash(password, salt))) {
            System.out.println("Submitted password is not a match");
            return null;
        }

        return user;
    }


    private String makePasswordHash(String password, String salt) {
        try {
            String saltedAndHashed = password + "," + salt;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(saltedAndHashed.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            byte hashedBytes[] = (new String(digest.digest(), "UTF-8")).getBytes();
            return encoder.encode(hashedBytes) + "," + salt;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 is not available", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 unavailable?  Not a chance", e);
        }
    }
}

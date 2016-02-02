## Sample Application for The Android Architecture Talk @ Android Dev Summit 2015.
[video][youtube]

This is a simple social sharing application where users can post text messages and also list other users' messages (feed).

It is written to demonstrate how the application can be designed to work offline and properly sync with the server as network becomes available, with minimal distraction to the user experience.


- [Disclaimers](#disclaimers)
- [How it works](#how-it-works)
- [Components](#components)
- [Data Flows](#data-flows)
  - [Sending a Post](#sending-a-post)
  - [Synchronizing Feeds](#synchronizing-feeds)
  - [Keeping The UI Up to Date](#keeping-the-ui-up-to-date)
- [Setup & Run & Tests](#setup-run-tests)
  - [Setup](#setup)
  - [Running](#running)
  - [Tests](#tests)
- [MISC](#misc)
  - [Avoiding Duplicate Posts](#avoiding-duplicate-posts)
- [License](#license)

### Disclaimers
**This is not an official Google product.**

* To be able to easily demonstrate server failures, the sample app ships with a simple Rails server. It is mostly scaffolded, has no security etc.
* The sample app uses many public open source projects. You **should not** take these as suggestions to use in your app. This demo
application is written in a short time and we've used many libraries to make it a complete app as fast as possible.
Since this is a demo app, we did not care much about performance characteristics of these libraries. As always, **do your due diligence** before using any library.
* The models do not cache anything in memory. A real app should.
* There is moderate testing for the project. They are not state of the art testing examples but show how different use cases can be tested.

### How it works
On the topic of "offline design", many solutions depend on the particular use case but are applicable to different scenarios with small modifications. As such, this demo has its own synching logic and may not 100% match your use case. You should consider it as an example, study and then figure out how to apply a similar approach to your application. Unfortunately, there is no
**one fits for all** solution for offline design.

Here, we'll explain how some of the user interaction flows work, which should give a better idea of what is going on in the application.

The sample project does not religiously follow any particular architectural pattern. Instead, it uses a hybrid approach that fits its own use case. It is designed with the assumption that it will grow into a large application (thus the complexity below).
We chose this approach to make the demo as useful and realistic as possible despite the added complexity.

### Components
* Value Objects:
  * These objects keep data, usually backed by the database. They also know how to validate themselves so that if the server sends invalid data (hello API change!), we can ignore it before it infects the model.
* Models:
  * These are responsible for persisting user data locally and providing methods to retrieve it.
* Controllers:
  * These are components to keep your main application logic. They decide how to do certain things (e.g. send a new post), react to GCM etc.
* Activities:
  * Activities control the user interface. They know where to load the data from or when to refresh themselves.
* Events:
  * The demo application uses a global EventBus. It is the only way for the application logic to notify the UI.
  * When the UI needs something from the application logic, it makes direct calls instead. This fits the overall design of Android where UI components are more ephemeral. This flow also avoids circular dependencies between the UI and background components.
* Jobs:
  * These are well defined operations that are typically (but not necessarily) network related. Dividing your application logic into jobs makes it much easier to test and scale. For instance, sending a new user post to the server is a Job, as well as syncing a user's feed with the server updates.


### Data Flows
#### Sending a Post
When a user hits the send button, the first 4 steps are:
1. Validate post.
2. Save necessary information to persistent storage about the post (a Job in this case).
3. Update the PostModel to include the new Post.
4. Dispatch an event about this new Post.

  (optional) 4.a. If the UI is visible, it updates itself after receiving the event.

Pay attention that the first steps **did not** include any steps requiring network connection yet we already have up to date information in the user interface and saved the necessary information to eventually sync the Post to server.

5. Here are the steps for the Job:

 ![Send Post Job Flow Diagram][send_post_job]

* Priority Job Queue takes care of persisting the job, back-off on failure etc so those details are omitted in the diagram.
* A real app should also integrate with **JobScheduler API** to ensure pending posts are sent after application is closed.
* When the Post Job fails, it might be a better idea to persist additional data to somewhere in your app so that next time user visits your app, you can notify them about what happened. The Demo shows a system notification if the FeedActivity is not visible.

#### Synchronizing Feeds
Synchronizing is managed by 3 components:
* **FeedModel**:
  * Keeps track of the latest FeedItem (Post) timestamp for each feed. This timestamp is used in two places:
    * Refreshing feed so that we only receive newer items.
    * Creating the local Post. It is important that the locally created Post shows up in the feed queries. The client's clock timestamp may not match the server timestamp so until the Post is synced to server, we assign a timestamp to the new post based on the newest post timestamp of the feed.
  * Provides methods to the UI to fetch feeds from database.
* **FeedController**:
  * Responsible for creating `FetchFeedJob`.
  * Listens for post upload failures and notifies the user with a system notification unless some other UI component (e.g. FeedActivity) handles the error first.
  * This sample app version is very basic. In a real app, this would probably handle refreshing the feed when a GCM push notification arrives and also include some logic to avoid refreshing the feed too frequently.
* **FetchFeedJob**:
  * Makes the actual API call to get the latest posts for a given feed. It is responsible for updating the model and dispatch necessary events.

#### Keeping The UI Up to Date
The interaction between background and UI is well defined.
* UI components make direct method calls when they need to get something done. (e.g. send a Post)
* UI reads data only from models.
* Background components notify events when they get something done (or fail to do so).

UI components take care of registering/unregistering to the EventBus depending on their lifecycles and since background components never directly reference UI components, we don't risk leaking them.

* This EventBus usage usually creates some edge cases where the UI misses some events and goes out of sync. The demo application avoids these edge cases using the following rules:
  * When its lifecycle starts, it first registers for events, then loads data from the model.
  * Any event that arrives while the  data is being loaded triggers another sync after data loading has completed.
  * All events arrive with a timestamp marking the oldest item in relation to that event. The UI uses this timestamp when accessing the model so that if the Items are inserted into the database in a different order, we still fetch them because the UI will use the oldest timestamp.
  * When it is stopped (e.g. `Activity#onStop`), it stops listening to events. If it comes back, it will do a full sync anyways so missing events in between is alright.

**This is not the only way:** The sample app uses a global **EventBus**. You can implement similar functionality using Rx or hand crafted listeners or any other similar technology.  As always, do your own evaluation for your application.


### Setup & Run & Tests

#### Setup
The demo ships with a simple server but you will need Ruby on Rails to run it.
The suggested way to install ruby is through [Ruby Version Manager][rvm].
After installing ruby and Rails, you can start the server as follows:
```
> cd server;
> bundle install;
> rake db:migrate RAILS_ENV=development;
> rake rails:update:bin;
```
This will install the dependencies of the application and also create the database.

#### Running
**Server**

```
> cd server;
> rails s
```
**Client**

The demo app uses the host machine address in an emulator environment by default. (`http://10.0.2.2:3000`) If you run it in an emulator, it should work just fine, if not, you can change that address in the settings menu or by directly changing `DemoConfig` class.

#### Tests

* Server: The server side does not have any tests because we simply don't care :)
* Client: You can run the tests for the client via:
    ```
    > cd client;
    > ./gradlew clean app:connectedCheck app:test
    ```

### MISC
#### Avoiding Duplicate Posts
Writing a mobile app means **making peace with unreliable network**. By using persistent jobs that run when network is available, the demo app does most of the work, but unfortunately it does not end there.
``
Under unreliable network conditions, our application may hit a case where the data is saved in the server side but we could never received the success response, so the application still thinks the item is not posted and it will retry. Or even worse, it may happen if our server is having troubles.

Normally, this retry would mean duplication of the item. There are multiple strategies to solve this issue. The demo application uses a unique `(userId, clientId)` tuple to avoid duplicates. Here is how it works:

* When client creates a post, it assigns it a unique `clientId` (`UUID.randomUUID().toString()`). This UUID, coupled with user's ID is designed to be unique both on the client side and the server side.
* When server receives a post, it checks if the tuple already exists and if so, instead of saving a new one, simply returns the existing item.
* When client fetches the feed, if a Post's `(userId, client Id)` tuple matches with an existing Post, it overrides it. This can happen if client saves a post, cannot receive the response but the Post shows up in another request's response.

You can play with these edge cases by toggling `error_before_saving_post` and `error_after_saving_post` in the `server/app/controllers/posts_controller.rb`.

### License
```
Copyright (C) 2015 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.

You may obtain a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and
limitations under the License.
```
[send_post_job]: https://www.evernote.com/shard/s19/sh/1106170f-4f73-4519-ae7d-4f4af13d4182/6ebed67e4a74a4a4/res/beafbb56-c5dc-4ecb-994f-30e06ab219a4/skitch.png
[rvm]:https://rvm.io/
[youtube]: https://www.youtube.com/watch?v=BlkJzgjzL0c

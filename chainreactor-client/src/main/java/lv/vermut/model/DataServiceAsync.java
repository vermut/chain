/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lv.vermut.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import lv.vermut.model.*;

import java.util.Date;
import java.util.List;

public interface DataServiceAsync {

    void authenticated(AsyncCallback<User> callback);

    void login(String login, String password, AsyncCallback<User> callback);

    void deviceLogin(String login, String password, AsyncCallback<Device> callback);

    void logout(AsyncCallback<Boolean> callback);

    void register(String login, String password, AsyncCallback<User> callback);

    void getUsers(AsyncCallback<List<User>> callback);

    void addUser(User user, AsyncCallback<User> callback);

    void updateUser(User user, AsyncCallback<User> callback);

    void removeUser(User user, AsyncCallback<User> callback);

    void getDevices(AsyncCallback<List<Device>> callback);

    void addDevice(Device device, AsyncCallback<Device> callback);

    void updateDevice(Device device, AsyncCallback<Device> callback);

    void removeDevice(Device device, AsyncCallback<Device> callback);

    void getLatestPositions(AsyncCallback<List<Position>> callback);

    void getPositions(Device device, Date from, Date to, AsyncCallback<List<Position>> callback);

    void updateApplicationSettings(ApplicationSettings applicationSettings, AsyncCallback<ApplicationSettings> callback);

    void getGameInfo(AsyncCallback<GameInfo> callback);

    void moveDevice(Device device, double offsetX, double offsetY, AsyncCallback<Boolean> callback);

    void getTeamReport(AsyncCallback<TeamReport> callback);

    void getDeviceReport(AsyncCallback<DeviceReport> callback);

    void attack(double lat, double lon, AsyncCallback<Boolean> callback);

    void getChatUrl(AsyncCallback<String> async);
}

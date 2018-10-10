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

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "positions", indexes = {
        @Index(name = "positionsIndex", columnList = "device_id, time")
})
public class Position implements Serializable, Cloneable {

    private static final long serialVersionUID = 1;

    public Position() {
    }

    public Position(Position position) {
        id = position.id;
        device = position.device;
        time = position.time;
        valid = position.valid;
        latitude = position.latitude;
        longitude = position.longitude;
        altitude = position.altitude;
        speed = position.speed;
        course = position.course;
        power = position.power;
        address = position.address;
        other = position.other;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public long getId() {
        return id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    private Device device;

    public Device getDevice() {
        return device;
    }

    private Date time;

    public Date getTime() {
        return time;
    }

    private Boolean valid;

    public Boolean getValid() {
        return valid;
    }

    private Double latitude;

    public Double getLatitude() {
        return latitude;
    }

    private Double longitude;

    public Double getLongitude() {
        return longitude;
    }

    private Double altitude;

    public Double getAltitude() {
        return altitude;
    }

    private Double speed;

    public Double getSpeed() {
        return speed;
    }

    private Double course;

    public Double getCourse() {
        return course;
    }

    private Double power;

    public Double getPower() {
        return power;
    }

    private String address;

    public String getAddress() {
        return address;
    }

    private String other;

    public String getOther() {
        return other;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}

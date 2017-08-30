package ru.highloadcup.travels.server;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import org.rapidoid.RapidoidThing;
import org.rapidoid.buffer.Buf;
import org.rapidoid.bytes.BytesUtil;
import org.rapidoid.data.BufRange;
import org.rapidoid.data.KeyValueRanges;
import org.rapidoid.http.HttpResponseCodes;
import org.rapidoid.http.MediaType;
import org.rapidoid.http.impl.HttpParser;
import org.rapidoid.http.impl.lowlevel.HttpIO;
import org.rapidoid.net.Protocol;
import org.rapidoid.net.Server;
import org.rapidoid.net.TCP;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;
import org.rapidoid.u.U;
import ru.highloadcup.travels.entity.Location;
import ru.highloadcup.travels.entity.User;
import ru.highloadcup.travels.entity.UsersVisit;
import ru.highloadcup.travels.entity.Visit;
import ru.highloadcup.travels.service.StorageService;
import ru.highloadcup.travels.wrapper.AverageMarkWrapper;
import ru.highloadcup.travels.wrapper.UsersVisitsWrapper;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class TravelsServer extends RapidoidThing implements Protocol {
    private final byte[] STATUS_200 = HttpResponseCodes.get(200);
    private static final byte[] HTTP_400 = fullResp(400, "Bad request!".getBytes());
    private static final byte[] HTTP_404 = fullResp(404, "Not found!".getBytes());
    private static final byte[] HTTP_500 = fullResp(500, "Error!".getBytes());
    private static final byte[] CONN_CLOSE_HDR = "Connection: close\r\n".getBytes();
    private static final byte[] CONTENT_TYPE_TXT = "Content-Type: ".getBytes();
    private static final byte[] EMPTY_RESP = { '{', '}' };

    private static final byte[] USERS = "/users/".getBytes();
    private static final BufRange USERS_GET_RANGE = new BufRange(4, 7);
    private static final BufRange USERS_POST_RANGE = new BufRange(5, 7);

    private static final byte[] LOCATIONS = "/locations/".getBytes();
    private static final BufRange LOCATIONS_GET_RANGE = new BufRange(4, 11);
    private static final BufRange LOCATIONS_POST_RANGE = new BufRange(5, 11);

    private static final byte[] VISITS = "/visits/".getBytes();
    private static final BufRange VISITS_GET_RANGE = new BufRange(4, 8);
    private static final BufRange VISITS_POST_RANGE = new BufRange(5, 8);

    private static final byte[] VISITS_END = "/visits".getBytes();
    private static final byte[] AVG = "/avg".getBytes();
    private static final byte[] NEW = "new".getBytes();

    private static final StorageService storageService = StorageService.INSTANCE;
    private final HttpParser HTTP_PARSER = new HttpParser();

    private static byte[] fullResp(int code, byte[] content) {
        String status = new String(HttpResponseCodes.get(code));
        String resp = status + "Content-Length: " + content.length + "\r\n\r\n" + new String(content);
        return resp.getBytes();
    }

    public void process(Channel ctx) {
        if (!ctx.isInitial()) {
            Buf buf = ctx.input();
            RapidoidHelper data = ctx.helper();
            this.HTTP_PARSER.parse(buf, data);
            boolean keepAlive = data.isKeepAlive.value;
            HttpStatus status = this.handle(ctx, buf, data);
            switch (status) {
            case DONE:
                ctx.closeIf(!keepAlive);
                break;
            case BAD_REQUEST:
                ctx.write(HTTP_400);
                ctx.closeIf(!keepAlive);
                break;
            case NOT_FOUND:
                ctx.write(HTTP_404);
                ctx.closeIf(!keepAlive);
                break;
            case ERROR:
                ctx.write(HTTP_500);
                ctx.closeIf(!keepAlive);
            }
        }
    }

    private HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper req) {
        if (req.isGet.value) {
            if (matches(buf, USERS_GET_RANGE, USERS)) {
                BufRange bufRange = new BufRange();
                try {
                    bufRange.set(getNextPositionAfter(USERS_GET_RANGE), req.path.length - USERS_GET_RANGE.length);
                    int id = (int) buf.getN(bufRange);
                    return getUser(id, ctx, req);
                } catch (RuntimeException ex) {
                    bufRange.set(getNextPositionAfter(req.path) - 7, 7);
                    if (matches(buf, bufRange, VISITS_END)) {
                        bufRange.set(getNextPositionAfter(USERS_GET_RANGE),
                                req.path.length - USERS_GET_RANGE.length - VISITS_END.length);
                        int id = (int) buf.getN(bufRange);
                        return getVisitsForUser(id, ctx, buf, req);
                    }
                    return HttpStatus.NOT_FOUND;
                }
            }
            if (matches(buf, LOCATIONS_GET_RANGE, LOCATIONS)) {
                BufRange bufRange = new BufRange();
                try {
                    bufRange.set(getNextPositionAfter(LOCATIONS_GET_RANGE), req.path.length - LOCATIONS_GET_RANGE.length);
                    int id = (int) buf.getN(bufRange);
                    return getLocation(id, ctx, req);
                } catch (RuntimeException ex) {
                    bufRange.set(getNextPositionAfter(req.path) - 4, 4);
                    if (matches(buf, bufRange, AVG)) {
                        bufRange.set(getNextPositionAfter(LOCATIONS_GET_RANGE),
                                req.path.length - LOCATIONS_GET_RANGE.length - AVG.length);
                        int id = (int) buf.getN(bufRange);
                        return getAvgMarkForLocation(id, ctx, buf, req);
                    }
                    return HttpStatus.NOT_FOUND;
                }
            }
            if (matches(buf, VISITS_GET_RANGE, VISITS)) {
                BufRange bufRange = new BufRange();
                try {
                    bufRange.set(getNextPositionAfter(VISITS_GET_RANGE), req.path.length - VISITS_GET_RANGE.length);
                    int id = (int) buf.getN(bufRange);
                    return getVisit(id, ctx, req);
                } catch (RuntimeException ex) {
                    return HttpStatus.NOT_FOUND;
                }
            }
        } else {
            if (matches(buf, USERS_POST_RANGE, USERS)) {
                BufRange bufRange = new BufRange();
                try {
                    bufRange.set(getNextPositionAfter(USERS_POST_RANGE), req.path.length - USERS_POST_RANGE.length);
                    int id = (int) buf.getN(bufRange);
                    return updateUser(id, ctx, buf, req);
                } catch (RuntimeException ex) {
                    bufRange.set(getNextPositionAfter(req.path) - 3, 3);
                    if (matches(buf, bufRange, NEW)) {
                        return createUser(ctx, buf, req);
                    }
                    return HttpStatus.NOT_FOUND;
                }
            }
            if (matches(buf, LOCATIONS_POST_RANGE, LOCATIONS)) {
                BufRange bufRange = new BufRange();
                try {
                    bufRange.set(getNextPositionAfter(LOCATIONS_POST_RANGE), req.path.length - LOCATIONS_POST_RANGE.length);
                    int id = (int) buf.getN(bufRange);
                    return updateLocation(id, ctx, buf, req);
                } catch (RuntimeException ex) {
                    bufRange.set(getNextPositionAfter(req.path) - 3, 3);
                    if (matches(buf, bufRange, NEW)) {
                        return createLocation(ctx, buf, req);
                    }
                    return HttpStatus.NOT_FOUND;
                }
            }
            if (matches(buf, VISITS_POST_RANGE, VISITS)) {
                BufRange bufRange = new BufRange();
                try {
                    bufRange.set(getNextPositionAfter(VISITS_POST_RANGE), req.path.length - VISITS_POST_RANGE.length);
                    int id = (int) buf.getN(bufRange);
                    return updateVisit(id, ctx, buf, req);
                } catch (RuntimeException ex) {
                    bufRange.set(getNextPositionAfter(req.path) - 3, 3);
                    if (matches(buf, bufRange, NEW)) {
                        return createVisit(ctx, buf, req);
                    }
                    return HttpStatus.NOT_FOUND;
                }
            }
        }
        return HttpStatus.NOT_FOUND;
    }


    private void startResponse(Channel ctx, boolean isKeepAlive) {
        ctx.write(this.STATUS_200);
        this.writeCommonHeaders(ctx, isKeepAlive);
    }

    private void writeCommonHeaders(Channel ctx, boolean isKeepAlive) {
        if (!isKeepAlive) {
            ctx.write(CONN_CLOSE_HDR);
        }
    }

    private void writeContentTypeHeader(Channel ctx, MediaType contentType) {
        ctx.write(CONTENT_TYPE_TXT);
        ctx.write(contentType.getBytes());
        ctx.write(CR_LF);
    }

    private void writeBody(Channel ctx, byte[] body, MediaType contentType) {
        this.writeContentTypeHeader(ctx, contentType);
        HttpIO.INSTANCE.writeContentLengthHeader(ctx, body.length);
        ctx.write(CR_LF);
        ctx.write(body);
    }

    private HttpStatus ok(Channel ctx, boolean isKeepAlive, byte[] body, MediaType contentType) {
        this.startResponse(ctx, isKeepAlive);
        this.writeBody(ctx, body, contentType);
        return HttpStatus.DONE;
    }

    private int getNextPositionAfter(BufRange bufRange) {
        return bufRange.start + bufRange.length;
    }

    private Map<String, String> getParams(Buf buf, RapidoidHelper req) {
        KeyValueRanges paramsKV = req.params.reset();
        HTTP_PARSER.parseParams(buf, paramsKV, req.query);
        return U.cast(paramsKV.toMap(buf, true, true, false));
    }

    private boolean matches(Buf buf, BufRange range, byte[] value) {
        return BytesUtil.matches(buf.bytes(), range, value, true);
    }

    public Server listen(int port) {
        return TCP.server().protocol(this).address("0.0.0.0").port(port).syncBufs(false).build().start();
    }

    private HttpStatus getUser(int id, Channel ctx, RapidoidHelper req) {
        final User user = storageService.findUserById(id);
        if (user == null) return HttpStatus.NOT_FOUND;
        return ok(ctx, req.isKeepAlive.value, JsonStream.serialize(user).getBytes(), MediaType.JSON);
    }

    private HttpStatus getLocation(int id, Channel ctx, RapidoidHelper req) {
        final Location location = storageService.findLocationById(id);
        if (location == null) return HttpStatus.NOT_FOUND;
        return ok(ctx, req.isKeepAlive.value, JsonStream.serialize(location).getBytes(), MediaType.JSON);
    }

    private HttpStatus getVisit(int id, Channel ctx, RapidoidHelper req) {
        final Visit visit = storageService.findVisitById(id);
        if (visit == null) return HttpStatus.NOT_FOUND;
        return ok(ctx, req.isKeepAlive.value, JsonStream.serialize(visit).getBytes(), MediaType.JSON);
    }

    private HttpStatus getAvgMarkForLocation(int id, Channel ctx, Buf buf, RapidoidHelper req) {
        if (!storageService.locationIsPresent(id)) {
            return HttpStatus.NOT_FOUND;
        }
        Map<String, String> params = getParams(buf, req);
        AverageMarkWrapper averageMarkWrapper = new AverageMarkWrapper();
        try {
            Long fromDate = null;
            if (params.containsKey("fromDate"))
                fromDate = Long.parseLong(params.get("fromDate"));
            Long toDate = null;
            if (params.containsKey("toDate"))
                toDate = Long.parseLong(params.get("toDate"));
            String gender = null;
            if (params.containsKey("gender")) {
                if (params.get("gender").length() != 1
                        || (!params.get("gender").equals("m") && !params.get("gender").equals("f")))
                    throw (new RuntimeException());
                else
                    gender = params.get("gender");
            }
            Long maxBirthdate = null;
            if (params.containsKey("fromAge")) {
                Integer fromAge = Integer.parseInt(params.get("fromAge"));
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                calendar.add(Calendar.YEAR, -fromAge);
                maxBirthdate = calendar.getTimeInMillis() / 1000;
            }
            Long minBirthdate = null;
            if (params.containsKey("toAge")) {
                Integer toAge = Integer.parseInt(params.get("toAge"));
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                calendar.add(Calendar.YEAR, -toAge);
                minBirthdate = calendar.getTimeInMillis() / 1000;
            }
            double averageMark = storageService.getAverageMarkForLocation(id, fromDate, toDate,
                    maxBirthdate, minBirthdate, gender);
            averageMarkWrapper.setAvg(averageMark);
        } catch (Exception ex) {
            return HttpStatus.BAD_REQUEST;
        }
        return ok(ctx, req.isKeepAlive.value, JsonStream.serialize(averageMarkWrapper).getBytes(), MediaType.JSON);
    }

    private HttpStatus getVisitsForUser(int id, Channel ctx, Buf buf, RapidoidHelper req) {
        if (!storageService.userIsPresent(id)) {
            return HttpStatus.NOT_FOUND;
        }
        Map<String, String> params = getParams(buf, req);
        UsersVisitsWrapper usersVisitsWrapper = new UsersVisitsWrapper();
        try {
            Long fromDate = null;
            if (params.containsKey("fromDate"))
                fromDate = Long.parseLong(params.get("fromDate"));
            Long toDate = null;
            if (params.containsKey("toDate"))
                toDate = Long.parseLong(params.get("toDate"));
            String country = null;
            if (params.containsKey("country"))
                country = params.get("country");
            Integer toDistance = null;
            if (params.containsKey("toDistance"))
                toDistance = Integer.parseInt(params.get("toDistance"));
            final List<UsersVisit> usersVisits = storageService.findUsersVisits(id, fromDate, toDate,
                    country, toDistance);
            usersVisitsWrapper.setVisits(usersVisits);
        } catch (Exception ex) {
            return HttpStatus.BAD_REQUEST;
        }
        return ok(ctx, req.isKeepAlive.value, JsonStream.serialize(usersVisitsWrapper).getBytes(), MediaType.JSON);
    }

    private HttpStatus updateUser(int id, Channel ctx, Buf buf, RapidoidHelper req) {
        if (!storageService.userIsPresent(id)) {
            return HttpStatus.NOT_FOUND;
        }
        try {
            String request = buf.get(req.body);
            Map <String,Any> params = JsonIterator.deserialize(request).asMap();
            String firstName = null;
            String lastName = null;
            String gender = null;
            Long birthdate = null;
            String email = null;
            for(Map.Entry<String, Any> param: params.entrySet()) {
                switch (param.getKey()) {
                case "email":
                    email = param.getValue().as(String.class);
                    if (email == null) throw new RuntimeException();
                    break;
                case "first_name":
                    firstName = param.getValue().as(String.class);
                    if (firstName == null) throw new RuntimeException();
                    break;
                case "last_name":
                    lastName = param.getValue().as(String.class);
                    if (lastName == null) throw new RuntimeException();
                    break;
                case "gender":
                    gender = param.getValue().as(String.class);
                    if (gender == null || gender.length() != 1 || (!gender.equals("m") && !gender.equals("f"))) throw new RuntimeException();
                    break;
                case "birth_date":
                    birthdate = param.getValue().as(Long.class);
                    if (birthdate == null) throw new RuntimeException();
                }
            }
            storageService.updateUser(id, firstName, lastName, gender, birthdate, email);
        } catch (Exception ex) {
            return HttpStatus.BAD_REQUEST;
        }
        return ok(ctx, req.isKeepAlive.value, EMPTY_RESP, MediaType.JSON);
    }

    private HttpStatus updateLocation(int id, Channel ctx, Buf buf, RapidoidHelper req) {
        if (!storageService.locationIsPresent(id)) {
            return HttpStatus.NOT_FOUND;
        }
        try {
            String request = buf.get(req.body);
            Map <String,Any> params = JsonIterator.deserialize(request).asMap();
            String country = null;
            String city = null;
            String place = null;
            Integer distance = null;
            for(Map.Entry<String, Any> param: params.entrySet()) {
                switch (param.getKey()) {
                case "place":
                    place = param.getValue().as(String.class);
                    if (place == null) throw new RuntimeException();
                    break;
                case "country":
                    country = param.getValue().as(String.class);
                    if (country == null) throw new RuntimeException();
                    break;
                case "city":
                    city = param.getValue().as(String.class);
                    if (city == null) throw new RuntimeException();
                    break;
                case "distance":
                    distance = param.getValue().as(Integer.class);
                    if (distance == null) throw new RuntimeException();
                }
            }
            storageService.updateLocation(id, country, city, place, distance);
        } catch (Exception ex) {
            return HttpStatus.BAD_REQUEST;
        }
        return ok(ctx, req.isKeepAlive.value, EMPTY_RESP, MediaType.JSON);
    }

    private HttpStatus updateVisit(int id, Channel ctx, Buf buf, RapidoidHelper req) {
        if (!storageService.visitIsPresent(id)) {
            return HttpStatus.NOT_FOUND;
        }
        try {
            String request = buf.get(req.body);
            Map <String,Any> params = JsonIterator.deserialize(request).asMap();
            Integer location = null;
            Integer user = null;
            Long visitedAt = null;
            Integer mark = null;
            for(Map.Entry<String, Any> param: params.entrySet()) {
                switch (param.getKey()) {
                case "location":
                    location = param.getValue().as(Integer.class);
                    if (location == null) throw new RuntimeException();
                    break;
                case "user":
                    user = param.getValue().as(Integer.class);
                    if (user == null) throw new RuntimeException();
                    break;
                case "visited_at":
                    visitedAt = param.getValue().as(Long.class);
                    if (visitedAt == null) throw new RuntimeException();
                    break;
                case "mark":
                    mark = param.getValue().as(Integer.class);
                    if (mark == null) throw new RuntimeException();
                }
            }
            storageService.updateVisit(id, location, user, visitedAt, mark);
        } catch (Exception ex) {
            return HttpStatus.BAD_REQUEST;
        }
        return ok(ctx, req.isKeepAlive.value, EMPTY_RESP, MediaType.JSON);
    }

    private HttpStatus createUser(Channel ctx, Buf buf, RapidoidHelper req) {
        try {
            String request = buf.get(req.body);
            User user = JsonIterator.deserialize(request, User.class);
            storageService.insertUser(user);
        } catch (Exception ex) {
            return HttpStatus.BAD_REQUEST;
        }
        return ok(ctx, req.isKeepAlive.value, EMPTY_RESP, MediaType.JSON);
    }

    private HttpStatus createLocation(Channel ctx, Buf buf, RapidoidHelper req) {
        try {
            String request = buf.get(req.body);
            Location location = JsonIterator.deserialize(request, Location.class);
            storageService.insertLocation(location);
        } catch (Exception ex) {
            return HttpStatus.BAD_REQUEST;
        }
        return ok(ctx, req.isKeepAlive.value, EMPTY_RESP, MediaType.JSON);
    }

    private HttpStatus createVisit(Channel ctx, Buf buf, RapidoidHelper req) {
        try {
            String request = buf.get(req.body);
            Visit visit = JsonIterator.deserialize(request, Visit.class);
            storageService.insertVisit(visit);
        } catch (Exception ex) {
            return HttpStatus.BAD_REQUEST;
        }
        return ok(ctx, req.isKeepAlive.value, EMPTY_RESP, MediaType.JSON);
    }

}

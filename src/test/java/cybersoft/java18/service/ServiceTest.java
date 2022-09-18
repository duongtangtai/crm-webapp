package cybersoft.java18.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import cybersoft.java18.jdbc.JDBCFunction;
import cybersoft.java18.model.ResponseData;
import cybersoft.java18.model.UserModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;


class ServiceTest extends JDBCFunction {
    Service service = ServiceHolder.getService();
    @Test
    void shouldGetGsonSuccessfully() {
        Gson myGson = new Gson();
        Gson serviceGson = service.getGson();
        Gson serviceGson2 = service.getGson();
        UserModel userModel = new UserModel().id(1).email("duongtangtai.gmail.com").password("1234");
        Assertions.assertEquals(myGson.toJson(userModel), serviceGson.toJson(userModel));
        Assertions.assertEquals(serviceGson, serviceGson2);
    }
    @Test
    void shouldCreateResponseDataSuccessfully() {
        ResponseData responseData = new ResponseData().statusCode(200).successful(true).message("Succeeded").content(null);
        ResponseData serviceResponseData = service.createResponseData(200, true, "Succeeded", null);
        Assertions.assertEquals(responseData, serviceResponseData);
    }
    @Test
    void shouldGetAlgorithmSuccessfully() {
        Algorithm algorithm = service.getAlgorithm();
        Algorithm algorithm1 = service.getAlgorithm();
        Assertions.assertEquals(algorithm, algorithm1);
        Assertions.assertEquals("HS256", service.getAlgorithm().getName());
    }
    @Test
    void shouldGetBCryptPasswordEncoderSuccessfully() {
        BCryptPasswordEncoder bCryptPasswordEncoder = service.getPasswordEncoder();
        BCryptPasswordEncoder bCryptPasswordEncoder1 = service.getPasswordEncoder();
        Assertions.assertEquals(bCryptPasswordEncoder, bCryptPasswordEncoder1);
    }
    @Test
    void shouldCreateTokenSuccessfully() {
        Date date = new Date(System.currentTimeMillis() + 10000);
        String token = JWT.create()
                .withSubject("1")
                .withIssuer("requestedURL")
                .withClaim("role", "MyRole")
                .withExpiresAt(date)
                .sign(service.getAlgorithm());
        String serviceToken = service.createToken("1", "requestedURL", "MyRole", date);
        Assertions.assertEquals(token, serviceToken);
    }
}

package com.aqzscn.www.global.controller;

import com.aqzscn.www.global.domain.co.AppException;
import com.aqzscn.www.global.domain.co.GlobalCaches;
import com.aqzscn.www.global.domain.dto.ReturnVo;
import com.aqzscn.www.global.mapper.Dispatch;
import com.aqzscn.www.global.util.JacksonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@Api(tags = "请求中转服务")
@RequestMapping("/g")
public class DispatchController extends BaseController {

    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(DispatchController.class);

    public DispatchController(HttpServletRequest request, HttpServletResponse response, RestTemplate restTemplate) {
        super(request, response);
        this.restTemplate = restTemplate;
    }

    @ApiModelProperty("中转post请求(仅支持POST JSON数据)")
    @PostMapping("/dispatch/**")
    public String post(@RequestBody String str) throws RuntimeException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Dispatch dispatch = GlobalCaches.DISPATCH;
            // 判断当前转发服务是否激活
            if (dispatch == null) {
                throw AppException.of("当前没有激活的转发服务，请激活后使用！");
            }
            // 判断是否需要处理请求数据
            String reqBody = str;
            if (StringUtils.isNotBlank(dispatch.getReqTargetParam())) {
                // 对于请求数据，是否需要获取具体数据（仅支持第一层对象）
                JsonNode node = objectMapper.readTree(str);
                String objStr = objectMapper.writeValueAsString(node.get(dispatch.getReqTargetParam()));
                if (StringUtils.isNotBlank(dispatch.getReqPrefix())) {
                    reqBody = "jsonRest=" + objStr;
                } else {
                    reqBody = objStr;
                }
            }
            // URL地址
            ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(dispatch.getServiceUrl(), reqBody, String.class);
            // 判断是否需要处理响应数据
            if (StringUtils.isNotBlank(dispatch.getResBody()) && StringUtils.isNotBlank(dispatch.getResDataKey())) {
                JsonNode node = objectMapper.readTree(dispatch.getResBody());
                Map<String, Object> map = new HashMap<>();
                Iterator<String> names = node.fieldNames();
                while (names.hasNext()) {
                    String name = names.next();
                    if (name.equals(dispatch.getResDataKey())) {
                        map.put(name, objectMapper.readTree(responseEntity.getBody()));
                    } else {
                        map.put(name, node.get(name));
                    }
                }
                return objectMapper.writeValueAsString(map);
            } else {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            throw AppException.of(e.getMessage());
        }
    }

    @PostMapping("/dispatch/user/reluser/byopenid")
    public CustomVo byopenid() throws RuntimeException {
        CustomVo vo = new CustomVo();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree("{\"RelUser\":{\"appid\":\"wx8a66989d34571ce0\",\"defaultrelapp\":\"1\",\"openid\":\"oBbT400YOp196crc2LPGslMfj0Ms\",\"relappid\":\"10\",\"relappname\":\"浙一\",\"reluserid\":\"262\"}}");
            vo.setData(node);
            vo.setCode(0);
            vo.setMsg("操作成功");
        } catch (Exception e) {
            vo.setCode(1000);
            vo.setMsg("处理失败");
        }
        return vo;
    }

    @PostMapping("/dispatch/relserver/getall")
    public CustomVo getall() throws RuntimeException {
        CustomVo vo = new CustomVo();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree("{\"relServerList\":[{\"relappid\":\"10\",\"relappname\":\"zy\",\"remark\":\"本地\",\"url\":\"http://127.0.0.1:82/cqm\"},{\"relappid\":\"11\",\"relappname\":\"ze\",\"remark\":\"本地\",\"url\":\"http://127.0.0.1:82/cqm\"},{\"relappid\":\"16\",\"relappname\":\"tt\",\"remark\":\"天坛\",\"url\":\"http://192.168.3.174:82/cqm/\"}]}");
            vo.setData(node);
            vo.setCode(0);
            vo.setMsg("操作成功");
        } catch (Exception e) {
            vo.setCode(1000);
            vo.setMsg("处理失败");
        }
        return vo;
    }

    @PostMapping("/dispatch/user/bindrelation")
    public CustomVo bind() {
        CustomVo vo = new CustomVo();
        vo.setCode(0);
        vo.setMsg("操作成功");
        return vo;
    }

    @PostMapping("/dispatch/user/unbindrelation")
    public CustomVo unbind() {
        CustomVo vo = new CustomVo();
        vo.setCode(0);
        vo.setMsg("操作成功");
        return vo;
    }

}

@NoArgsConstructor
@AllArgsConstructor
@Data
class CustomVo {

    private Integer code;

    private String msg;

    private Object data;

}

//@NoArgsConstructor
//@AllArgsConstructor
//@Data
//class CqmVo {
//
//    private String errorCode;
//
//    private String message;
//
//    private Object return;
//
//}
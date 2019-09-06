package com.example.running.controller;

import com.example.running.bean.JsonResult;
import com.example.running.bean.ResultUtils;
import com.example.running.dao.WebsiteDao;
import com.example.running.dto.WebsiteAddDTO;
import com.example.running.dto.WebsiteEditDTO;
import com.example.running.dto.WebsiteResultDTO;
import com.example.running.entity.Website;
import com.example.running.utils.WebsiteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@EnableScheduling
public class MainController {

    @Autowired
    WebsiteDao websiteDao;

    @Autowired
    private JavaMailSender mailSender;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("")
    public ModelAndView index(HttpServletRequest request){

        ModelAndView modelAndView=new ModelAndView();
        modelAndView.setViewName("home");

        return modelAndView;
    }


    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JsonResult getWebsiteList(){

        List<Website> websiteList=websiteDao.findAll();
        List<WebsiteResultDTO> resultDTOList=new ArrayList<>();
        for(Website website:websiteList){
            WebsiteResultDTO websiteResultDTO=new WebsiteResultDTO();
            BeanUtils.copyProperties(website,websiteResultDTO);
            resultDTOList.add(websiteResultDTO);
        }
        return ResultUtils.success(resultDTOList);

    }

    //一定要是value="",否则静态资源会访问不了！！
    @RequestMapping(value = "/websiteRecord",method = RequestMethod.POST)
    public JsonResult addRecord(@RequestBody WebsiteAddDTO recordAddDTO){

        recordAddDTO.setAddress(recordAddDTO.getAddress().trim());
        if(websiteDao.findFirstByAddress(recordAddDTO.getAddress())==null){
            Website website=new Website();
            BeanUtils.copyProperties(recordAddDTO,website);
            Website newWebsite=websiteDao.insert(website);

            check(newWebsite);

            return ResultUtils.success();
        }
        else{
            return ResultUtils.error(-1,"该网站已存在");
        }



    }


    @RequestMapping(value = "/websiteRecord",method = RequestMethod.DELETE)
    public JsonResult deleteWebsite(@RequestParam("id") String id){

        Website website=websiteDao.findFirstById(id);
        websiteDao.delete(website);

        return ResultUtils.success();
    }

    @RequestMapping(value = "/websiteRecord",method = RequestMethod.PUT)
    public JsonResult editWebsite(@RequestBody WebsiteEditDTO websiteEditDTO){

        Website website=websiteDao.findFirstById(websiteEditDTO.getId());
        BeanUtils.copyProperties(websiteEditDTO,website);
        websiteDao.save(website);

        check(website);

        return ResultUtils.success();
    }

    @RequestMapping(value = "/test",method = RequestMethod.POST)
    public JsonResult test(@RequestParam("id") String id){

        Website website=websiteDao.findFirstById(id);

        int code=check(website);

        if(code==200||code==302){
            sendEmail(website.getEmail(),website.getAddress()+"\n网站访问正常，code:"+code+"\nTimeStamp:"+new Date().getTime());
        }

        return ResultUtils.success();
    }

    private int check(Website website){
        int code=WebsiteUtils.visitWebSite(website.getAddress());
        website.setStatus(code);
        websiteDao.save(website);

        logger.info(website.getAddress()+"  "+dateFormat.format(new Date())+"  "+code);

        if(code!=200&&code!=302){
            sendEmail(website.getEmail(),website.getAddress()+"\n网站无法访问，code:"+code+"\nTimeStamp:"+new Date().getTime());
        }
        else{
//            sendEmail(website.getEmail(),website.getAddress()+"\n网站访问正常，code:"+code);
        }
        return code;
    }

    SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "0 0/15 * * * *")  // 表示 在指定时间执行
//    @Scheduled(cron = "0/10 * * * * *")  // 表示 在指定时间执行
    private void fixTimeExecution() {
        List<Website> websiteList=websiteDao.findAll();
        Date date=new Date();
        int min=date.getMinutes();
        int hour=date.getHours();
        for(int i=0;i<websiteList.size();i++){
            Website website=websiteList.get(i);
            switch(website.getInterval()){
                case "15min":
                    check(website);
                    break;
                case "30min":
                    if(min==30||min==0){
                        check(website);
                    }
                    break;
                case "1h":
                    if(min==0){
                        check(website);
                    }
                    break;
                case "6h":
                    if(min==0&&(hour==9||hour==15||hour==21||hour==3)){
                        check(website);
                    }
                    break;
                case "12h":
                    if(min==0&&(hour==9||hour==21)){
                        check(website);
                    }
                    break;
                case "24h":
                    if(min==0&&(hour==9)){
                        check(website);
                    }
                    break;
            }
        }

        logger.info("在指定时间 " + dateFormat.format(date) + "执行完成");
    }

    private void sendEmail(String recipient,String content){

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("nj_gis@163.com");

        message.setTo(recipient);

        message.setSubject("OpenGMS 网站访问性测试");

        message.setText(content);

        try {

            mailSender.send(message);


        } catch (Exception e) {

            logger.error("发送邮件时发生异常了！", e);

        }
    }

}

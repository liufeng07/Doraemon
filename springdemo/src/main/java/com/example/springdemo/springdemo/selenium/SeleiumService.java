package com.example.springdemo.springdemo.selenium;

import com.example.springdemo.springdemo.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @program: springBootPractice
 * @description:
 * @author: hu_pf
 * @create: 2020-05-15 17:30
 **/
@Service
public class SeleiumService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SeleniumConfig seleniumConfig;

    @Autowired
    @Qualifier("xPath")
    private ClickInterface xPath;

    @Autowired
    @Qualifier("className")
    private ClickInterface className;

    private static List<String> ALL_PHONES = new ArrayList<>();

    public static WebDriver driver;

    static {
        driver = new FirefoxDriver();
    }

    /**
    * @Description: 所有文章点赞
    * @Param: []
    * @return: void
    * @Author: hu_pf
    * @Date: 2020/6/2
    */
    public void allPages(){
        sleep(1000);
        driver.get("https://juejin.im/user/5b7286a76fb9a009c624342f");
        driver.findElement(By.xpath("/html/body/div/div[2]/main/div[3]/div[1]/div[2]/div/div[1]/div/a[2]/div[1]")).click();
        login("13425190352");
        scroll("//div[contains(@class, 'row abstract-row')]/a[1]","不学无数——SpringBoot入门Ⅰ");
        List<WebElement> noAvtice = driver.findElements(By.xpath("//span[contains(@class, 'count likedCount')]"));
        List<WebElement> active = driver.findElements(By.xpath("//span[contains(@class, 'count likedCount active')]"));
        boolean b = noAvtice.removeAll(active);
        sleep(1000);
        noAvtice.forEach(webElement -> {
            sleep(10000);
            webElement.click();
        });
    }

    /**
    * @Description: 单篇文章点赞
    * @Param: [url]
    * @return: void
    * @Author: hu_pf
    * @Date: 2020/6/2
    */
    public void pages(String url){
        List<String> allPhones = seleniumConfig.getAllPhones();
        String exitPhones = userMapper.selectExitPhone(url);
        List<String> exitPhonesList = new ArrayList<>();
        if (!StringUtils.isEmpty(exitPhones)){
            exitPhonesList = Arrays.asList(exitPhones.split(SeleiumConstants.SPLIT));
        }else {
            exitPhones = StringUtils.EMPTY;
        }
        allPhones.removeAll(exitPhonesList);
        int i = 0;
        for (String s : allPhones) {
            if (i == SeleiumConstants.LIMIT_NUM){
                break;
            }
            sleep(3000);
            driver.get(url);
            login(s);
            try {
                click("//*[@class=\"like-btn panel-btn like-adjust with-badge\"]",driver,()->xPath);
                i++;
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("点过赞了");
            }
            exitPhones = insertOrUpdate(url,exitPhones,s).toString();
            exit();
        }
    }

    private StringBuilder insertOrUpdate(String url, String exitPhones,String phone){
        StringBuilder stringBuilder = new StringBuilder(exitPhones);
        stringBuilder.append(SeleiumConstants.SPLIT+phone);
        ThumbsUpRecordDto thumbsUpRecordDto = ThumbsUpRecordDto.builder()
                .url(url)
                .phones(stringBuilder.toString())
                .build();
        if (StringUtils.isEmpty(exitPhones)){
            userMapper.insertIntoThumbsUpRecord(thumbsUpRecordDto);
        }else {
            userMapper.updateIntoThumbsUpRecord(thumbsUpRecordDto);
        }
        return stringBuilder;
    }

    public StringBuilder insertOrUpdate(String url, String exitPhones,String phone,String name){
        StringBuilder stringBuilder = new StringBuilder(exitPhones);
        stringBuilder.append(SeleiumConstants.SPLIT+phone);
        ThumbsUpRecordDto thumbsUpRecordDto = ThumbsUpRecordDto.builder()
                .url(url)
                .phones(stringBuilder.toString())
                .userName(name)
                .build();
        if (StringUtils.isEmpty(exitPhones)){
            userMapper.insertIntoThumbsUpRecord(thumbsUpRecordDto);
        }else {
            userMapper.updateIntoThumbsUpRecord(thumbsUpRecordDto);
        }
        return stringBuilder;
    }


    /**
    * @Description: 评论中内容点赞
    * @Param: [url]
    * @return: void
    * @Author: hu_pf
    * @Date: 2020/6/2
    */
    public void comment(String url,String name,FeiDian invoke) {
        String exitPhones = userMapper.selectExitPhoneAndName(url,name);
        ALL_PHONES = seleniumConfig.getAllPhones();
        List<String> exitPhonesList = new ArrayList<>();
        if (!StringUtils.isEmpty(exitPhones)){
            exitPhonesList = Arrays.asList(exitPhones.split(","));
        }else {
            exitPhones = StringUtils.EMPTY;
        }
        Integer maxNum = 0;
        ALL_PHONES.removeAll(exitPhonesList);
        for (String s : ALL_PHONES) {
            driver = new FirefoxDriver();
            driver.get(url);
            if (maxNum == SeleiumConstants.COMMENT_MAX){
                break;
            }
            login(s);
            maxNum = invoke.invoke(name, maxNum);
            exitPhones = insertOrUpdate(url,exitPhones,s,name).toString();
//                exit();
            driver.close();
        }
    }

    /**
    * @Description: 评论本身点赞
    * @Param: []
    * @return: void
    * @Author: hu_pf
    * @Date: 2020/7/10
    */
    public void commentSend(){

    }

    public WebElement getWebElement(WebElement element,String xpath){
        List<WebElement> elements = element.findElements(By.xpath(xpath));
        if (elements.size() == 0){
            return null;
        }else {
            return elements.get(0);
        }
    }

    /**
    * @Description: 获取评论中最高的赞
    * @Param: [url]
    * @return: void
    * @Author: hu_pf
    * @Date: 2020/6/10
    */
    public void getCommentMax(String url) throws Exception{
        driver.get(url);
        login("18983773470");
        WebDriverWait wait = new WebDriverWait(driver,10,1);
        sleep(1000);
        String num = driver.findElement(By.xpath("//div[contains(@class, 'action-box sticky')]/div[2]")).getText();
        if (num.contains("评论")){
            System.out.println(num);
        }
        scroll("//*[@class=\"user-content-box\"]","//*[@class=\"content-box comment-divider-line\"]",Integer.valueOf(num));
        List<WebElement> elements = driver.findElements(By.xpath("//div[@class='like-action action']/span"));
        List<Integer> commentList = new ArrayList<>();

        for (WebElement element : elements) {
            if (!StringUtils.isEmpty(element.getText())){
                commentList.add(Integer.valueOf(element.getText()));
            }
        }

        commentList.stream().sorted().forEach(System.out::println);
//        driver.close();
    }



    /**
    * @Description: 退出登录
    * @Param: []
    * @return: void
    * @Author: hu_pf
    * @Date: 2020/6/2
    */
    public void exit(){
        click("//*[@id=\"juejin\"]/div[2]/div/header/div/nav/ul/li[5]/div",driver,()->xPath);
        click("//*[@id=\"juejin\"]/div[2]/div/header/div/nav/ul/li[5]/ul/div[4]/li",driver,()->xPath);
        driver.switchTo().alert().accept();
    }

    public void login(String userName){

        // 点击登录按钮
        click("login",driver,()->className);
        // 账号
        driver.findElement(By.name("loginPhoneOrEmail")).sendKeys(userName);
        // 密码
        driver.findElement(By.name("loginPassword")).sendKeys("hpy911213");
        // 登录按钮
        click("//*[@id=\"juejin\"]/div[1]/div[3]/form/div[2]/button",driver,()->xPath);
        sleep(1000);
    }

    private void click(String name, WebDriver driver, Supplier<ClickInterface> click){
        click.get().click(name,driver);
    }


    /**
    * @Description: 向下翻滚页面,评论翻滚
    * @Param: [comment, sonComment, stopNum]
    * @return: void
    * @Author: hu_pf
    * @Date: 2020/5/15
    */
    public void scroll(String comment,String sonComment,Integer stopNum){
        Boolean flag = true;
        int i = 1;
        Integer befCommentSize = 0;
        Integer commentSize = 0;
        while (flag){
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,150000)");
            befCommentSize = commentSize;
            List<WebElement> elements = driver.findElements(By.xpath(comment));
            List<WebElement> elements2 = driver.findElements(By.xpath(sonComment));
            sleep(500);
            commentSize = elements.size()+elements2.size();
            if (befCommentSize.intValue() == commentSize.intValue()){
                i++;
            }
            if ((befCommentSize.intValue() == commentSize.intValue()&&i>5)||stopNum.intValue() == commentSize){
                flag = false;
            }
        }
    }


    /**
    * @Description: 向下翻滚页面
    * @Param: [xPath, stop]
    * @return: void
    * @Author: hu_pf
    * @Date: 2020/5/15
    */
    public void scroll(String xPath,String stop){
        Boolean flag = true;
        int bef = 0;
        int aft = 0;
        while (flag){
            List<WebElement> elements = driver.findElements(By.xpath(xPath));
            bef = elements.size();
            sleep(500);
            if (aft != bef){
                for (WebElement webElement : elements){
                    String text = webElement.getText();
                    if (stop.equals(text)){
                        flag = false;
                    }
                }
                aft = bef;
            }
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,50000)");
        }
    }


    public void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

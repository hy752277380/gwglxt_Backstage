package com.kcsj.gwglxt.controller.DocumentManage;

import com.kcsj.gwglxt.DTO.DocumentCustom;
import com.kcsj.gwglxt.DTO.LoginCustom;
import com.kcsj.gwglxt.DTO.MessageCustom;
import com.kcsj.gwglxt.entity.*;
import com.kcsj.gwglxt.service.documentManage.DocumentService;
import com.kcsj.gwglxt.util.TeamUtil;
import com.kcsj.gwglxt.vo.QueryForPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.Process;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class DocumentManageController {
    @Autowired
    private DocumentService documentService;

    /**********************************************************文档添加查询及流程模块********************************************/
    //获取全部文档
    @RequestMapping("/getAllDocument")
    public QueryForPage getAllDocument(String fuzzySearch, int currentPage,String documentType,Integer documentConfidential,String documentDept, HttpSession httpSession) {
        QueryForPage queryForPage = null;
        try {
            //获取session内容
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            String departmentName = loginCustom.getDepartment().getDepartmentName();
            String userId = loginCustom.getGuser().getUserId();
            queryForPage = documentService.getAllDocument(departmentName, userId, currentPage, fuzzySearch,documentType,documentConfidential,documentDept);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryForPage;
    }

    //添加文档
    @RequestMapping("/addDocument")
    public Document addDocument(@RequestBody Document document, HttpSession httpSession, HttpServletResponse response) {
        String result;
        int addResult = 0;
        try {
            //获取session内容
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            //初始化result
            //从对象中获得文档标题
            String documentName = document.getDocumentTitle();
            //将部分属性存入doc对象中
            document.setDocumentId(TeamUtil.getUuid());
            document.setDocumentDept(loginCustom.getGuser().getUserDepartment());
            document.setDocumentUser(loginCustom.getGuser().getUserId());
            document.setDocumentConfidential(document.getDocumentConfidential());
            document.setDoucmentContent(document.getDoucmentContent());
            document.setDocumentRemark(document.getDocumentRemark());
            document.setDocumentProcess(document.getDocumentProcess());
            document.setDocumentLocation(0);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            document.setCreationTime(df.format(new Date()));
            document.setDocumentState(2);
            document.setDocumentSpeed(document.getDocumentSpeed());
            document.setDocumentIsdelete(0);
            //将对象存入数据库
            addResult = documentService.insert(document);
            Log log = new Log();
            //存入日志信息到日志表
            log.setLogId(TeamUtil.getUuid());
            log.setLogUser(loginCustom.getGuser().getUserId());
            log.setLogContent("添加了" + documentName + "文档");
            log.setCreationTime(df.format(new Date()));
            documentService.insertLog(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (addResult == 0) {
            result = "updateFailed";
        }else{
            result = "updateSuccess";
        }
        return document;
    }

    //修改文档
    @RequestMapping("/updateDocument")
    public Document updateDocument(@RequestBody Document document,HttpSession httpSession) {
        try {
            String result = null;
            //获取session中的信息
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            //添加操作日志
            Log log = new Log();
            log.setLogId(TeamUtil.getUuid());
            log.setCreationTime(df.format(new Date()));
            log.setLogUser(loginCustom.getGuser().getUserId());
            //根据职位id获取职位名称
            log.setLogContent("修改了"+document.getDocumentTitle()+"文档内容。");
            documentService.insertLog(log);
            document.setDocumentLocation(0);
            document.setDocumentState(2);
            document.setCreationTime(df.format(new Date()));
            document.setDocumentIsdelete(0);
            document.setDocumentUser(loginCustom.getGuser().getUserId());
            document.setDocumentDept(loginCustom.getGuser().getUserDepartment());
            documentService.updateByPrimaryKey(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    //提交文档，更改文档状态
    @RequestMapping("/updateDocumentState")
    public String updateDocumentState(@RequestBody Document document, HttpSession httpSession, HttpServletResponse response) {
        String result = null;
        int updateResult = 0;
        try {
            //初始化result
            result = null;
            //获取session中的信息
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            //根据文档id修改文档状态
            String documentId = document.getDocumentId();
            //定义当前文档类型
            Integer documentState = 3;
            //根据得到的文档id获得文档信息
            Document document1 = documentService.selectByPrimaryKey(documentId);
            //更新文档状态为2
            String documentProcessBegin = null;
            String documentProcessFinish = null;
            updateResult = documentService.updateDocumentState(documentState, documentProcessBegin, documentProcessFinish, documentId);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            Log log = new Log();
            String text = null;
            text = "提交了" + document1.getDocumentTitle() + "文档";
            log.setLogId(TeamUtil.getUuid());
            log.setLogUser(loginCustom.getGuser().getUserId());
            log.setLogContent(text);
            log.setCreationTime(df.format(new Date()));
            int addLogResult = documentService.insertLog(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (updateResult == 0) {
            result = "updateFailed";
        }else{
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }

    //更改文档当前所处流程的位置
    @RequestMapping("/updateDocumentLocation")
    public String updateDocumentLocation(String documentId, HttpSession httpSession, HttpServletResponse response) {
        String result = null;
        int updateLocationResult = 0;
        try {
            //初始化result
            result = null;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            //获取session内容
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            Document document = documentService.selectByPrimaryKey(documentId);
            //生成审核人日志文件
            Log log = new Log();
            log.setLogId(TeamUtil.getUuid());
            log.setLogUser(loginCustom.getGuser().getUserId());
            log.setLogContent("审核了" + document.getDocumentTitle() + "公文");
            log.setCreationTime(df.format(new Date()));
            documentService.insertLog(log);
            //生成被审核人的日志文件
            Log log1 = new Log();
            log1.setLogId(TeamUtil.getUuid());
            log1.setLogUser(document.getDocumentUser());
            log1.setLogContent("您的" + document.getDocumentTitle() + "被" + loginCustom.getGuser().getUserName() + "审核了");
            log1.setCreationTime(df.format(new Date()));
            documentService.insertLog(log1);
            int documentLocation = document.getDocumentLocation() + 1;
            //获取当前所走流程的最大步骤
            int maxStep = documentService.getMaxStep(document.getDocumentProcess());
            String documentProcessBegin = null;
            String documentProcessFinish = null;
            //根据流程所处不同节点改变文档状态：当前流程所处节点为1是文档状态改为3，当前流程所处节点位置为最后一位是文档状态改为4
            if (documentLocation == 1) {
                documentProcessBegin = df.format(new Date());
                documentService.updateDocumentState(4, documentProcessBegin, documentProcessFinish, documentId);
            } else if (documentLocation == maxStep) {
                documentProcessFinish = df.format(new Date());
                documentService.updateDocumentState(5, documentProcessBegin, documentProcessFinish, documentId);
                Message message = new Message();
                String messageId = TeamUtil.getUuid();
                message.setMessageId(messageId);
                message.setMessageContent("您提交的公文" + document.getDocumentTitle() + "已经全部审核完毕了！");
                message.setMessageTime(df.format(new Date()));
                message.setMessageIsdelete(0);
                message.setMessageType(4);
                documentService.insertMsg(message);
                Mobject mobject = new Mobject();
                mobject.setMobjectId(TeamUtil.getUuid());
                mobject.setMobjectUser(document.getDocumentUser());
                mobject.setMobjectMessage(messageId);
                mobject.setMobjectIsread(0);
                documentService.insertMbj(mobject);
            }
            updateLocationResult = documentService.updateDocumentLocation(documentLocation, documentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (updateLocationResult == 0) {
            result = "updateFailed";
        } else {
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }

    //通知下一个节点操作人
    @RequestMapping("/messageNextOne")
    public String messageNextOne(String documentId) {
        String result;
        int messageResult = 0;
        try {
            messageResult = documentService.insertMessage(documentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageResult == 0) {
            result = "updateFailed";
        } else {
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }
    //申请人发起退回
    @RequestMapping("/callBack")
    public String callBack(String documentId,HttpSession httpSession){
        String result;
        int updayeResult = 0;
        try {
            //获取session内容
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            updayeResult = documentService.callBack(loginCustom, documentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (updayeResult == 0) {
            result = "updateFailed";
        } else {
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }
    //文档审核选择拒绝
    @RequestMapping("/refuseDoc")
    public String refuseDoc(String documentId,String refuseReason, HttpSession httpSession) {
        String result;
        int updayeResult = 0;
        try {
            //获取session内容
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            updayeResult = documentService.refuseDoc(loginCustom, documentId,refuseReason);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (updayeResult == 0) {
            result = "updateFailed";
        } else {
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }

    //根据文档状态查询文档
    @RequestMapping("/getDocumentByState")
    public QueryForPage getDocumentByState(String userId,String documentType, Integer documentConfidential, Integer documentState, int currentPage, String fuzzySearch, HttpSession httpSession) {
        QueryForPage queryForPage = null;
        try {
            if(userId==null||"".equals(userId)){
                //获取session内容
                LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
                userId=loginCustom.getGuser().getUserId();
            }
            queryForPage = documentService.getDocumentByState(documentType, documentConfidential, documentState, userId, currentPage, fuzzySearch);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryForPage;
    }

    //根据id查看文档全部信息
    @RequestMapping("/documentBaseInfo")
    public DocumentCustom documentBaseInfo(String documentId) {
        DocumentCustom documentCustom = null;
        try {
            documentCustom = documentService.documentBaseInfo(documentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentCustom;
    }

    //查看文档的所有流程节点
    @RequestMapping("/getProcessNode")
    public List<ProcessNode> getProcessNode(String documentId) {
        List<ProcessNode> list = null;
        try {
            Document document = documentService.selectByPrimaryKey(documentId);
            list = documentService.getAllProcessNode(document.getDocumentProcess());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    //根据流程id流程子节点
    @RequestMapping("/getProcessNodeByPro")
    public List<ProcessNode> getProcessNodeByPro(String process){
        List<ProcessNode> processNodes = null;
        try {
            processNodes = documentService.getProcessNodeByPro(process);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  processNodes;
    }

    //查询本人需要审核的文档
    @RequestMapping("/findCheckDoc")
    public QueryForPage findCheckDoc(String fuzzySearch,int currentPage,String documentType,Integer documentConfidential,String documentDept, HttpSession httpSession) {
        QueryForPage queryForPage = null;
        try {
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            queryForPage = documentService.findCheckingDoc(currentPage, loginCustom,fuzzySearch,documentType,documentConfidential,documentDept);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryForPage;
    }
    //删除草稿箱文档
    @RequestMapping("/deleteDoc")
    public String deleteDoc(@RequestParam("ids[]") String[] ids,HttpSession httpSession){
        String result;
        int updateResult = 0;
        try {
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            updateResult = documentService.deleteDoc(ids,loginCustom);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (updateResult == 0) {
            result = "updateFailed";
        } else {
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }

    /***************************文档借阅部分*****************************/

    //列出待本人批准的借阅申请
    @RequestMapping("/getAllApplyRead")
    public QueryForPage getAllApplyRead(String fuzzySearch,int currentPage,String documentType,Integer documentConfidential,String userDpt,HttpSession httpSession) {
        QueryForPage queryForPage = null;
        try {
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            queryForPage = documentService.getAllApplyRead(loginCustom,currentPage,documentType,documentConfidential,userDpt,fuzzySearch);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryForPage;
    }

    //申请批阅文档
    @RequestMapping("/applyRead")
    public String applyRead(@RequestBody DocumentCustom documentCustom, HttpSession httpSession) {
        String result;
        DocumentCustom documentCustomNew = null;
        try {
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            documentCustomNew = documentService.insertBorrowing(documentCustom,loginCustom);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(documentCustomNew!=null){
            result = "applySuccess";
            return "{\"msg\":\"" + result + "\"}";
        }else {
            result = "applyFailed";
            return "{\"msg\":\"" + result + "\"}";
        }
    }

    //同意借阅文档
    @RequestMapping("/acceptApply")
    public String acceptApply(@RequestBody  DocumentCustom documentCustom, HttpSession httpSession) {
        String result;
        int updateResult = 0;
        try {
            //定义result
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            updateResult = documentService.acceptApply(documentCustom,loginCustom);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (updateResult == 0) {
            result = "updateFailed";
        } else {
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }

    //拒绝借阅文档
    @RequestMapping("/refuseApply")
    public String refuseApply(@RequestBody  DocumentCustom documentCustom,String refuseReason, HttpSession httpSession) {
        String result;
        int updateResult = 0;
        try {
            //定义result
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            updateResult = documentService.refuseApply(documentCustom,loginCustom,refuseReason);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (updateResult == 0) {
            result = "updateFailed";
        } else {
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }

    /**********************************日志消息中心及其他*********************************/

    //获取所有本人消息
    @RequestMapping("/getAllMessage")
    public QueryForPage getMyAllMessage(int currentPage,HttpSession httpSession) {
        QueryForPage queryForPage = null;
        try {
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            queryForPage = documentService.getMyAllMessage(loginCustom.getGuser().getUserId(),currentPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryForPage;
    }
    //获得本人未读消息
    @RequestMapping("/getUnReadMsg")
    public List<MessageCustom> getUnReadMsg(HttpSession httpSession) {
        LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
        List<MessageCustom> list = documentService.getUnReadMsg(loginCustom.getGuser().getUserId());
        return list;
    }
    //标记消息已读
    @RequestMapping("/isRead")
    public String isRead(String MobjectId){
        String result;
        int updateResult = 0;
        try {
            updateResult = documentService.isRead(MobjectId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (updateResult == 0) {
            result = "updateFailed";
        } else {
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }
    //一键消息已读
    @RequestMapping("/allAreRead")
    public String allAreRead(HttpSession httpSession){
        String result;
        int updateResult = 0;
        try {
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            updateResult = documentService.allAreRead(loginCustom.getGuser().getUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (updateResult == 0) {
            result = "updateFailed";
        } else {
            result = "updateSuccess";
        }
        return "{\"msg\":\"" + result + "\"}";
    }

    //查看本人所有日志
    @RequestMapping("/getAllLog")
    public List<List<Log>> getAllLog(int year, String userId,HttpSession httpSession) {
        List<List<Log>> logs  = null;
        try {
            LoginCustom loginCustom = (LoginCustom) httpSession.getAttribute("LoginInformation");
            if (userId==null||"".equals(userId)){
                userId = loginCustom.getGuser().getUserId();
            }
            logs = documentService.getLog(year,userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    //获取所有公文种类
    @RequestMapping("/getAllDocType")
    public List<Documenttype> getAllDocType() {
        List<Documenttype> list = null;
        try {
            list = documentService.getAllDocType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //获取所有流程
    @RequestMapping("/getAllProcess")
    public QueryForPage getAllProcess(int currentPage,String fuzzySearch) {
        QueryForPage queryForPage = null;
        try {
            queryForPage = documentService.getAllProcess(currentPage,fuzzySearch);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryForPage;
    }
    //获取所有流程（不分页）
    @RequestMapping("/getAllProcessNoPage")
    public List<com.kcsj.gwglxt.entity.Process> getAllProcessNoPage() {
        List<com.kcsj.gwglxt.entity.Process> processes = null;
        try {
            processes = documentService.getAllProcessNoPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processes;
    }

    //登录
    @PostMapping("/login")
    public String login() {
        return "{" +
                "  \"code\": 20000," +
                "  \"data\": {" +
                "    \"token\": \"admin\"" +
                "  }" +
                "}";
    }

    @GetMapping("/loginAnother")
    public String loginAnther() {
        return "{" +
                "  \"code\": 20000," +
                "  \"data\": {" +
                "    \"roles\": [\"admin\"]," +
                "    \"name\": \"admin\"," +
                "    \"avatar\": \"https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif\"" +
                "  }" +
                "}";
    }
}

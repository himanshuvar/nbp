package com.opensds.esdk.controller;

import com.opensds.esdk.bean.ErrorCode;
import com.opensds.esdk.model.ResultInfo;
import com.opensds.esdk.model.VCenterInfo;
import com.opensds.esdk.service.VCenterService;
import com.opensds.esdk.utils.FileUtils;
import com.opensds.esdk.utils.MediaTypeUtils;
import com.opensds.esdk.utils.OperationSystemUtils;
import com.opensds.esdk.utils.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


@Controller
public class IndexController implements  ServletContextAware {

    @Autowired
    private VCenterService vCenterService;

    private ServletContext servletContext;

    private static final Logger logger = LogManager.getLogger(IndexController.class);

    private static final String ERROR_PAGE = "error";

    private static final String SUCCESS_PAGE = "success";

    private static final String RESULT_INFO = "resultInfo";

    private static final String Home_Page = "homePage";

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * direct to home page
     * @param model
     * @return
     */
    @RequestMapping(value = "/homePage")
    public String home (Model model) {
        VCenterInfo vcInfo = vCenterService.getvCenterInfo();
        model.addAttribute("VCenterInfo", vcInfo);
        return Home_Page;
    }

    /**
     * direct to register action
     * @param vcInfo
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/action", method = RequestMethod.POST, params = "action=register")
    public String registerSubmit (@Valid @ModelAttribute VCenterInfo vcInfo,
                                 BindingResult bindingResult,
                                 Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(RESULT_INFO, new ResultInfo(ErrorCode.CONNECT_FAIL.getErrodCode().intValue(),
                    ErrorCode.CONNECT_FAIL.getErrorDESC()));
            return ERROR_PAGE;
        }
        ResultInfo result = vCenterService.registerPlugin(vcInfo);
        if ((boolean)result.getData() == true) {
            model.addAttribute(RESULT_INFO, "Register success!");
            return SUCCESS_PAGE;
        } else {
            model.addAttribute(RESULT_INFO, result.getErrorDesc());
            return ERROR_PAGE;
        }
    }

    /**
     * direct to unregister aciton
     * @return
     */
    @RequestMapping(value = "/action", method = RequestMethod.POST, params = "action=unregister")
    public String doUnregister (@Valid @ModelAttribute VCenterInfo vcInfo,
                                 BindingResult bindingResult,
                                 Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(RESULT_INFO, new ResultInfo(ErrorCode.CONNECT_FAIL.getErrodCode().intValue(),
                    ErrorCode.CONNECT_FAIL.getErrorDESC()));
            return ERROR_PAGE;
        }
        ResultInfo result = vCenterService.unregisterPlugin(vcInfo);
        if ((boolean)result.getData() == true) {
            model.addAttribute(RESULT_INFO, "Unregister success!");
            return SUCCESS_PAGE;
        } else {
            model.addAttribute(RESULT_INFO, result.getErrorDesc());
            return ERROR_PAGE;
        }
    }

    /**
     * get the ngc zip file
     * @param fileName
     * @return
     * @throws IOException
     */
    @RequestMapping(value="/download/{fileName}", method= RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadFile (@PathVariable("fileName") String fileName)
            throws IOException {
        logger.info(String.format("-----------------Begin download the zip file %s-----------------", fileName));
        if (!vCenterService.isValidZipFileName(fileName)) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, fileName);
        File file = new File(vCenterService.getZipfilePath());
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                // Content-Type
                .contentType(mediaType)
                // Contet-Length
                .contentLength(file.length())
                .body(resource);
    }

    /**
     * TEST LOG
     * @return
     */
    @ResponseBody
    @GetMapping("/mylog")
    private String myLog() {
        logger.debug("BASE_PATH:" + XmlUtils.BASE_PATH + "\n\r");
        logger.debug("FileUtils.getBasePath :"+ FileUtils.getBasePath());
        logger.debug("FileUtils.getParentPath :"+ FileUtils.getParentPath());
        logger.debug("FileUtils.getConfigPath :"+ FileUtils.getConfigPath());
        OperationSystemUtils.isFreeDiskSpace();
        return "FileUtils.getBasePath :  "+ FileUtils.getBasePath() + "<hr>" +
                 "FileUtils.getParentPath :  " + FileUtils.getParentPath() + "<hr>" +
                "FileUtils.directPath :  " + FileUtils.getDirectPath() + "<hr>" +
                "FileUtils.projectPath :  " + FileUtils.getProjectPath() + "<hr>" +
                "FileUtils.classPath :  " + FileUtils.getClassPath() + "<hr>" +
                "FileUtils.jarPath :  " + FileUtils.getJarPath() + "<hr>" +
                "FileUtils.getConfigPath :   " + FileUtils.getConfigPath();
    }
}
package com.chenyanwu.erp.erpframework.service.impl.importutil;

import com.chenyanwu.erp.erpframework.dao.importutil.ErpStudentExcelMapper;
import com.chenyanwu.erp.erpframework.dao.importutil.ErpStudentMapper;
import com.chenyanwu.erp.erpframework.entity.importutil.ErpStudent;
import com.chenyanwu.erp.erpframework.entity.importutil.ErpStudentExcel;
import com.chenyanwu.erp.erpframework.entity.importutil.StudentExcelImport;
import com.chenyanwu.erp.erpframework.exception.BusinessException;
import com.chenyanwu.erp.erpframework.service.impl.BaseServiceImpl;
import com.chenyanwu.erp.erpframework.service.importutil.ErpStudentService;
import com.chenyanwu.erp.erpframework.service.importutil.ImportTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author chenyanwu
 * @date 2019-04-26 12:37:18
 */
@Service
public class ErpStudentServiceImpl extends BaseServiceImpl<ErpStudent, Object>
        implements ErpStudentService {

    private final Logger logger = LoggerFactory.getLogger(ErpStudentServiceImpl.class);

    @Autowired
    private ErpStudentMapper erpstudentMapper;

    @Autowired
    public void setMapper() {
        super.setMapper(erpstudentMapper);
    }

    @Autowired
    public ErpStudentExcelMapper excelMapper;

    @Override
    public List<ErpStudent> selectByExample(Object var1) {
        logger.info("当前的条件值为：[{}]" + var1);
        return erpstudentMapper.selectByExample(var1);
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public Integer importDataReturn(List<StudentExcelImport> list) {
        int errorCounts = 0;
        List<ErpStudent> erpStudents = new ArrayList<>();
        List<ErpStudentExcel> erpStudentExcels = new ArrayList<>();
        for (StudentExcelImport data : list) {
            StringBuffer errorReason = varifyImport(data);
            if (errorReason.toString().equals("")) {
                ErpStudent erpStudent = new ErpStudent();
                erpStudent.setName(data.getName());
                erpStudent.setAge(data.getAge());
                erpStudent.setAddress(data.getAddress());
                erpStudents.add(erpStudent);
            } else {
                ErpStudentExcel erpStudentExcel = new ErpStudentExcel();
                erpStudentExcel.setName(data.getName());
                erpStudentExcel.setAge(data.getAge());
                erpStudentExcel.setAddress(data.getAddress());
                erpStudentExcel.setReason(errorReason.toString() + "为空");
                erpStudentExcels.add(erpStudentExcel);
                errorCounts++;
            }
        }

        if (erpStudents.size() > 0) {
            erpstudentMapper.insertList(erpStudents);
        }
        List<ErpStudentExcel> ErpStudentExcelsTab = excelMapper.selectAll();
        if (ErpStudentExcelsTab.size() > 0) {
            excelMapper.batchDel(ErpStudentExcelsTab);
        }

        if (erpStudentExcels.size() > 0) {
            excelMapper.insertList(erpStudentExcels);
        }

        return errorCounts;
    }

    //验证必填项是否有空值，将失败原因写入reason字段
    public StringBuffer varifyImport(StudentExcelImport studentExcelImport) {
        StringBuffer errorReason = new StringBuffer("");
        if (StringUtils.isEmpty(studentExcelImport.getName())) {
            errorReason.append("\"姓名\"");
        }
        if (StringUtils.isEmpty(studentExcelImport.getAge())) {
            errorReason.append("\"年龄\"");
        }
        if (StringUtils.isEmpty(studentExcelImport.getAddress())) {
            errorReason.append("\"地址\"");
        }

        return errorReason;
    }

    @Override
    public List<StudentExcelImport> findStudentErrorExcel() {
        List<ErpStudentExcel> erpStudentExcels = excelMapper.selectAll();
        List<StudentExcelImport> studentExcelImports = new ArrayList<>();
        for (ErpStudentExcel data : erpStudentExcels) {
            StudentExcelImport studentExcelImport = new StudentExcelImport();
            studentExcelImport.setName(data.getName());
            studentExcelImport.setAge(data.getAge());
            studentExcelImport.setAddress(data.getAddress());
            studentExcelImports.add(studentExcelImport);
        }
        return studentExcelImports;

    }

    @Override
    public void importDataNoReturn(List<StudentExcelImport> list) {

    }
}

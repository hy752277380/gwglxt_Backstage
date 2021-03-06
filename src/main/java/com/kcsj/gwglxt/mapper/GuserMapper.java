package com.kcsj.gwglxt.mapper;


import java.util.List;

import com.kcsj.gwglxt.DTO.CountByMouth;
import com.kcsj.gwglxt.entity.Guser;
import com.kcsj.gwglxt.entity.GuserExample;
import com.kcsj.gwglxt.DTO.LoginCustom;
import org.apache.ibatis.annotations.Param;

public interface GuserMapper {
    int countByExample(GuserExample example);

    int deleteByExample(GuserExample example);

    int deleteByPrimaryKey(String userId);

    int insert(Guser record);

    int insertSelective(Guser record);

    List<Guser> selectByExample(GuserExample example);

    Guser selectByPrimaryKey(String userId);

    int updateByExampleSelective(@Param("record") Guser record, @Param("example") GuserExample example);

    int updateByExample(@Param("record") Guser record, @Param("example") GuserExample example);

    int updateByPrimaryKeySelective(Guser record);

    int updateByPrimaryKey(Guser record);

    LoginCustom loginInfo(String userId);
    LoginCustom loginFunction(String userAccount);
    //根据职位查询人员
    List<Guser> getUserByPosition(@Param("userPosition") String userPosition,@Param("userDepartment") String userDepartment);
    //查询个人信息
    LoginCustom getPersonalInfo(String userId);

    int countByDepartment(String departmentId);

    List<Guser> getDptManager(@Param("userDepartment") String userDepartment,@Param("userPosition") String userPosition);

    CountByMouth countUserByMouth(String year);

    int countAllUser();

    List<LoginCustom> getAllUser(@Param("fuzzySearch") String fuzzySearch,@Param("userId") String userId);

    List<LoginCustom> getUserByDpt(@Param("userDepartment") String userDepartment,@Param("fuzzySearch") String fuzzySearch,@Param("userId") String userId);

    List<Guser> getUserByAcc(@Param("userAccount") String userAccount);
}
package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {

    //营业额统计
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    //用户数量统计
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    //订单数量统计
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    //查找销量排名前10
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    //导出Excel报表接口
    void exportExcel(HttpServletResponse httpServletResponse);
}

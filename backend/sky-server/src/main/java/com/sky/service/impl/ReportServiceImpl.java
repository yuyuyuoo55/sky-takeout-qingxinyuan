package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceServiceImpl workspaceService;

    //营业额统计
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //日期集合 2025-11-2,2025-11-3,...2025-11-9
        List<LocalDate> localDateList = dateStatistics(begin, end);

        List<Integer> turnoverList=new ArrayList<>();
        //遍历日期集合，获取每天的营业额25.0,34.0,...45.3
        for (LocalDate date : localDateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer turnover=orderMapper.getFinishOrdersByCheckoutTime(beginTime,endTime);//获取每天的营业额
            if(turnover==null)
            {
                turnoverList.add(0);
            }
            else
            turnoverList.add(turnover);
        }

        //将日期集合变为字符串类型
        String dateList = StringUtils.join(localDateList, ",");

        //将营业额集合变为字符串类型
        String ListTurnover = StringUtils.join(turnoverList, ",");

        TurnoverReportVO vo = TurnoverReportVO.builder()
                .dateList(dateList)
                .turnoverList(ListTurnover)
                .build();

        return vo;
    }

    //用户数量统计
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //获取日期列表
        List<LocalDate> dateList = dateStatistics(begin, end);

        //用户新增列表
        List<Integer> newUserList=new ArrayList<>();
        //总用户列表
        List<Integer> totalUserList=new ArrayList<>();


        Map map=new HashMap();
        //统计用户列表
        for (LocalDate date : dateList) {
            //当天最大时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            map.put("endTime",endTime);
            //获取用户创建时间<当天最大时间的总用户数量
            totalUserList.add(userMapper.countByMap(map));

            //当天最小时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            map.put("beginTime",beginTime);
            newUserList.add(userMapper.countByMap(map));
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    //订单数量统计
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = dateStatistics(begin, end);

        //订单数量
        List<Integer> orderCountList=new ArrayList<>();
        //有效订单数
        List<Integer> validOrderCountList=new ArrayList<>();

        //订单总数 select count(id) from orders where order_time<
        Integer totalOrderCount=0;
        //有效订单数
        Integer validOrderCount=0;
        //订单完成率
        Double orderCompletionRate=0.00;

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map=new HashMap();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);

            //获取订单数量
            Integer orderNum = orderMapper.countByMap(map);
            totalOrderCount+=orderNum;
            orderCountList.add(orderNum);

            //获取有效订单数量
            map.put("status",5);
            Integer vaildOrderNum = orderMapper.countByMap(map);
            validOrderCount+=vaildOrderNum;
            validOrderCountList.add(vaildOrderNum);
        }

        //避免除零错误，并正确计算完成率
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalOrderCount)
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .dateList(StringUtils.join(dateList,","))
                .build();
    }

    //查找销量排名前10
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //查询销售top10
        List<GoodsSalesDTO> list = orderMapper.getTop10(beginTime, endTime);
        
        //商品名称列表，以逗号分隔，例如：鱼香肉丝,宫保鸡丁,水煮鱼
        List<String> name = new ArrayList<>();

        //销量列表，以逗号分隔，例如：260,215,200
        List<Integer> number = new ArrayList<>();

        for (GoodsSalesDTO salesDTO : list) {
            // 添加空值检查，防止 NullPointerException
            if (salesDTO != null) {
                name.add(salesDTO.getName());
                number.add(salesDTO.getNumber());
            }
        }

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(name,","))
                .numberList(StringUtils.join(number,","))
                .build();
    }

    //导出Excel报表接口
    public void exportExcel(HttpServletResponse httpServletResponse) {
        //1.查询概览数据
        LocalDate now = LocalDate.now();
        LocalDate beginTime = now.minusDays(30);

        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(beginTime, LocalTime.MIN), LocalDateTime.of(now, LocalTime.MIN));

        //优化日期格式
        String begin = beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String end = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //2.获取报表模板
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(is);

            //3.写入数据
            //将日期数据写入模板(2,2)
            XSSFSheet sheet = excel.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue("日期: "+begin+" 至 "+end);

            //写入营业额(4,3)
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            //写入订单完成率(4,5)
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            //写入新增用户数(4,7)
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            //写入有效订单数(5,3)
            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            //写入平均客单价(5,5)
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            //填充明细数据
            int i=7;
            while(!beginTime.equals(now))
            {
                //写入日期(8,2)
                sheet.getRow(i).getCell(1).setCellValue(beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                //获取当天数据
                businessData = workspaceService.getBusinessData(LocalDateTime.of(beginTime, LocalTime.MIN), LocalDateTime.of(beginTime, LocalTime.MAX));

                //写入营业额(8,3)
                sheet.getRow(i).getCell(2).setCellValue(businessData.getTurnover());
                //写入订单完成率(8,4)
                sheet.getRow(i).getCell(3).setCellValue(businessData.getOrderCompletionRate());
                //写入新增用户数(8,5)
                sheet.getRow(i).getCell(4).setCellValue(businessData.getNewUsers());
                //写入有效订单数(8,6)
                sheet.getRow(i).getCell(5).setCellValue(businessData.getValidOrderCount());
                //写入平均客单价(8,7)
                sheet.getRow(i).getCell(6).setCellValue(businessData.getUnitPrice());

                beginTime=beginTime.plusDays(1);
                i++;
            }

            //将文件写出到浏览器中
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            excel.write(outputStream);

            //关闭流对象
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //获取日期列表
    private static List<LocalDate> dateStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);

        while(!begin.equals(end))
        {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        return dateList;
    }
}
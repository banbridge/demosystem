package com.bupt.demosystem.easyexcel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.bupt.demosystem.util.EasyExcelFileUtil;
import com.bupt.demosystem.util.Group;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @Author banbridge
 * @Classname TestEasyExcel
 * @Date 2022/5/9 17:24
 */
public class TestEasyExcel {


    @Test
    public void test() {
        String fileName = EasyExcelFileUtil.getPath() + "tw" + System.currentTimeMillis() + ".xlsx";
        System.out.println(fileName);
        List<List<Object>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<Object> data = new ArrayList<>();
            data.add("张三" + i);
            data.add(25);
            data.add(new Date());
            list.add(data);
        }
        List<List<String>> list2 = new ArrayList<>();
        List<String> head0 = new ArrayList<>();
        head0.add("姓名");
        List<String> head1 = new ArrayList<>();
        head1.add("年撒打算大撒龄");
        List<String> head2 = new ArrayList<>();
        head2.add("生都打算打算的撒的撒日");
        list2.add(head0);
        list2.add(head1);
        list2.add(head2);

        EasyExcel
                .write(fileName)
                .head(list2)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("模版")
                .doWrite(list);
    }


}

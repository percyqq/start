package org.learn;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
public class 详情去重之增删改 {

    public static void main(String[] args) {


        List<AttachmentDetail> dbDetails = new ArrayList<>();
        //dbDetails = attachmentMapper.selectByExaminationId(examinationId);
        // db数据 L2 , L3, L5, L6

        List<AttachmentDetail> expectedAttachmentPos = Lists.newArrayList();
        // expectedAttachmentPos = { 提交的数组s L1, L2, L3}


        Equator attachEquator = new Equator<AttachmentDetail>() {
            @Override
            public boolean equate(AttachmentDetail t0, AttachmentDetail t1) {
                if (Objects.equals(t0.getKey(), t1.getKey()) && Objects.equals(t0.getValue(), t1.getValue())) {
                    return true;
                }
                return false;
            }

            @Override
            public int hash(AttachmentDetail attachmentPo) {
                return 0;
            }
        };

        //add attachment
        Collection<AttachmentDetail> inserts = CollectionUtils.removeAll(expectedAttachmentPos, dbDetails, attachEquator);
        if (!CollectionUtils.isEmpty(inserts)) {
            log.info(" attach upload, insert count : {}", inserts.size());
            //inserts.forEach(attachmentPo -> attachmentMapper.insert(attachmentPo));
        }

        //delete attachment
        Collection<AttachmentDetail> deletes = CollectionUtils.removeAll(dbDetails, expectedAttachmentPos, attachEquator);
        if (!CollectionUtils.isEmpty(deletes)) {
            log.info(" attach upload, delete count : {}", deletes.size());
            deletes.forEach(attachmentPo -> {
                //attachmentPo.setStatus(ValidEnum.INVALID);
                //attachmentMapper.updateById(attachmentPo);
            });
        }

    }
}


@Data
@AllArgsConstructor
class AttachmentDetail {
    private String key;
    private String name;
    private String value;
    private String url;

}
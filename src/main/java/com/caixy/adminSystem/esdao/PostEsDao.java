package com.caixy.adminSystem.esdao;

import com.caixy.adminSystem.model.dto.post.PostEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 帖子 ES 操作
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long>
{

    List<PostEsDTO> findByUserId(Long userId);
}
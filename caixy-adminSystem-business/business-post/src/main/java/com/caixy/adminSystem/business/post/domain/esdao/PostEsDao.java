package com.caixy.adminSystem.business.post.domain.esdao;

import com.caixy.adminSystem.business.post.domain.post.dto.PostEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 帖子 ES 操作
 *
 * @Author CAIXYPROMISE
 * @since 2024/12/26 1:40
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long>
{

    List<PostEsDTO> findByUserId(Long userId);
}
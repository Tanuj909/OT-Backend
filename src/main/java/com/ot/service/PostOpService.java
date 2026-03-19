package com.ot.service;

import com.ot.dto.postOp.PostOpResponse;
import com.ot.dto.postOp.PostOpTransferRequest;
import com.ot.dto.postOp.PostOpUpdateRequest;

public interface PostOpService {
    PostOpResponse getPostOpRecord(Long operationId);
    PostOpResponse updatePostOpRecord(Long operationId, PostOpUpdateRequest request);
    PostOpResponse transferPatient(Long operationId, PostOpTransferRequest request);
}
package service;

import util.message.MessageWrapper;

/**
 * Created by lifengshuang on 11/05/2017.
 */
public interface Service {
    byte[] handle(MessageWrapper request) throws Exception;
}

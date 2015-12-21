(ns ^:no-doc onyx.lifecycles.lifecycle-invoke
  (:require [clojure.core.async :refer [>!!]]))

(defn restartable-invocation [event phase handler-fn f & args]
  (try
    (apply f args)
    (catch Throwable t
      (let [action (handler-fn event phase t)]
        (cond (= action :kill)
              (throw t)

              (= action :restart)
              (do (>!! (:onyx.core/restart-ch event) true)
                  (throw t))

              :else
              (throw (ex-info
                      (format "Internal error, cannot handle exception with policy %s, must be one of #{:kill :restart :defer}"
                              action)
                      {})))))))

(defn invoke-start-task [event]
  (restartable-invocation
   event
   :lifecycle/start-task?
   (:onyx.core/compiled-handle-exception-fn event)
   (:onyx.core/compiled-start-task-fn event)
   event))

(defn invoke-before-task-start [event]
  (merge
   event
   (restartable-invocation
    event
    :lifecycle/before-task-start
    (:onyx.core/compiled-handle-exception-fn event)
    (:onyx.core/compiled-before-task-start-fn event)
    event)))

(defn invoke-before-batch [compiled-lifecycle event]
  (merge
   event
   (restartable-invocation
    event
    :lifecycle/before-batch
    (:onyx.core/compiled-handle-exception-fn event)
    compiled-lifecycle
    event)))

(defn invoke-after-read-batch [event]
  (merge
   event
   (restartable-invocation
    event
    :lifecycle/after-read-batch
    (:onyx.core/compiled-handle-exception-fn event)
    (:onyx.core/compiled-after-read-batch-fn event)
    event)))

(defn invoke-after-batch [event]
  (merge
   event
   (restartable-invocation
    event
    :lifecycle/after-batch
    (:onyx.core/compiled-handle-exception-fn event)
    (:onyx.core/compiled-after-batch-fn event)
    event)))

(defn invoke-after-ack [event compiled-lifecycle message-id ack-rets]
  (merge
   event
   (restartable-invocation
    event
    :lifecycle/after-ack-segment
    (:onyx.core/compiled-handle-exception-fn event)
    compiled-lifecycle
    event
    message-id
    ack-rets)))

(defn invoke-after-retry [event compiled-lifecycle message-id complete-rets]
  (merge
   event
   (restartable-invocation
    event
    :lifecycle/after-retry-segment
    (:onyx.core/compiled-handle-exception-fn event)
    compiled-lifecycle
    event
    message-id
    complete-rets)))

(defn invoke-after-task-stop [event]
  ;; This function intentionally does not execute
  ;; the lifecycle as a restartable-invocation. If
  ;; the task reaches this stage, it is already considered
  ;; complete. Restarting the task would cause the peer
  ;; to deadlock.
  (merge event ((:onyx.core/compiled-after-task-fn event) event)))

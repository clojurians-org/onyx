(ns ^:no-doc onyx.static.logging)

(defn log-prefix [task-info]
  (format "Job %s %s - Task %s %s - Peer %s -"
          (:job-id task-info)
          (:metadata task-info)
          (:task-id task-info)
          (:task-name task-info)
          (:id task-info)))

(defn merge-error-keys
  ([e task-info]
   (merge-error-keys e task-info ""))
  ([e task-info msg]
   (let [d (ex-data e)
         ks [:job-id :metadata :task-id :id]
         helpful-keys {:job-id (:job-id task-info)
                       :metadata (:metadata task-info)
                       :task-id (:task-id task-info)
                       :task-name (:name (:task task-info))
                       :peer-id (:id task-info)}
         error-keys (merge helpful-keys d)
         msg (format "%s -> Exception type: %s. Exception message: %s"
                     msg
                     ;; Uses the long form to get the class name on purpose.
                     ;; (str (type ...)) prefixes "class" in the string.
                     (.getName ^java.lang.Class (.getClass ^clojure.lang.ExceptionInfo e))
                     (.getMessage ^clojure.lang.ExceptionInfo e))]
     (ex-info msg error-keys e))))

(defn exception-msg [task-information msg]
  (str (log-prefix task-information) msg))

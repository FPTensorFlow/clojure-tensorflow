(ns clojure-tensorflow.core
  (:require [clojure-tensorflow.build :as build]
            [clojure-tensorflow.ops :as tf]
            [clojure-tensorflow.utils :as utils]))

(defn session
  "Create a session"
  ([graph] (new org.tensorflow.Session graph))
  ([] (session build/default-graph)))

(defn feed
  "Feed value to placeholder
  Pass a map of locations to values"
  ([runner feed-map]
   (utils/thread
     runner
     (map (fn [[key val]]
            #(.feed % key val)) feed-map))))

(defn op-run
  "Call session runner on single op.
  Returns tensor object"
  ([op] (op-run build/default-graph op))
  ([graph op] (op-run graph (session graph) op {}))
  ([graph session op] (op-run graph session op {}))
  ([graph session op feed-map]
  (-> session
      .runner
      (feed feed-map)
      (.fetch (.name (.op op)))
      .run
      (.get 0)
      )))


(defn session-run
  "Run list of ops, return last"
  ([ops] (session-run build/default-graph ops))
  ([graph ops] (session-run graph (session graph) ops))
  ([graph session ops]
   (let [ops (flatten ops)
         op-run (partial op-run graph session)]

     ;; run first n ops to set up state
     (doseq [op (butlast ops)]
       (op-run op))

     ;; run final op
     (let [result
           (utils/tensor->clj
            (op-run (last ops)))]
       ;; close session to free up memory
       (.close session)
       ;; return result
       result))))

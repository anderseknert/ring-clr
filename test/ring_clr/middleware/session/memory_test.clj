(ns ring-clr.middleware.session.memory-test
  (:require [clojure.test :refer [deftest is]]
            [ring-clr.middleware.session.store :refer [delete-session read-session write-session]]
            [ring-clr.middleware.session.memory :refer [memory-store]]))

(deftest memory-session-read-not-exist
  (let [store (memory-store)]
    (is (nil? (read-session store "non-existent")))))

(deftest memory-session-create
  (let [store    (memory-store)
        sess-key (write-session store nil {:foo "bar"})]
    (is (not (nil? sess-key)))
    (is (= (read-session store sess-key)
           {:foo "bar"}))))

(deftest memory-session-update
  (let [store     (memory-store)
        sess-key  (write-session store nil {:foo "bar"})
        sess-key* (write-session store sess-key {:bar "baz"})]
    (is (= sess-key sess-key*))
    (is (= (read-session store sess-key)
           {:bar "baz"}))))

(deftest memory-session-delete
  (let [store    (memory-store)
        sess-key (write-session store nil {:foo "bar"})]
    (is (nil? (delete-session store sess-key)))
    (is (nil? (read-session store sess-key)))))

(deftest memory-session-custom-atom
  (let [session  (atom {})
        store    (memory-store session)
        sess-key (write-session store nil {:foo "bar"})]
    (is (= (@session sess-key) {:foo "bar"}))
    (swap! session assoc sess-key {:foo "baz"})
    (is (= (read-session store sess-key)
           {:foo "baz"}))))

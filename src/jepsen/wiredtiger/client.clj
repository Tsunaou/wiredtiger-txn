(ns jepsen.wiredtiger.client
  (:require [clojure.tools.logging :refer [info warn]])
  (:import (com.wiredtiger.db wiredtiger
                              Connection
                              Session
                              Cursor
                              WiredTigerException)))
;; Connection managerment
;; Atom to record the connection to WiredTiger
(def wt-conn (atom {:conn nil}))

(defn ^Connection open
  "Opens a connection to a WiredTiger database."
  [dir]
  (let [_ (info "Get the connection to" dir)
        conn (wiredtiger/open dir "create")]
    conn))

(defn close-connection
  "Close a connection to a WiredTiger database."
  [^Connection conn]
  (let [_ (info "Close connection ")]
    (.close conn nil)))

(defn close-session
  "Close a session"
  [^Session session]
  (let [_ (info "Close session")]
    (.close session nil)))

(defn ^Session start-session
  "Start a new session"
  [^Connection conn]
  (let [_ (info "Start a new session on conn " conn)
        iso (new String "isolation=snapshot")
        session (.open_session conn iso)
        _ (info "Session is " session)]
    session))

(defn create-table-from-session
  [^Session session table-name table-format]
  (let [_ (info "Create a new table (if does not exist) named" table-name)]
    (.create session table-name table-format)))

(defn create-table
  "Create a new table (if does not exist)"
  [^Connection conn table-name table-format]
  (let [_ (info "Begin create-table")
        session (start-session conn)]
    (create-table-from-session session table-name table-format)))

(defn ^Cursor get-cursor
  [^Session session table-name]
  (.open_cursor table-name nil "overwrite=false" session))

(defn begin-transaction
  "Begin a transaction on a WiredTiger session."
  [^Session session isolation-level]
  (let [_ (info "begin transaction")]
    (.begin_transaction session isolation-level)))

(defn commit-transaction
  "Commit a transaction on a WiredTiger session."
  [^Session session]
  (let [_ (info "commit transaction")]
    (.commit_transaction session nil)))

(defn rollback-transaction
  "Rollback a transaction on a WiredTiger session."
  [^Session session]
  (let [_ (info "rollback transaction")]
    (.rollback_transaction session nil)))

;; Error handing
(defmacro with-errors
  "Remaps common errors; takes an operation and returns a :fail or :info op
  when a throw occurs in body."
  [op & body]
  `(try ~@body
     (catch WiredTigerException e#
       (throw e#))))
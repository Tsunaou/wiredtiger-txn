(ns jepsen.wiredtiger.client
  (:require [clojure.tools.logging :refer [info warn]])
  (:import (com.wiredtiger.db wiredtiger
                              Connection
                              Session
                              Cursor
                              WiredTigerException)))

;; Connection managerment
(defn ^Connection open
  "Opens a connection to a WiredTiger database."
  [dir]
  (info "Get the connection to" dir)
  (wiredtiger/open dir "create"))

(defn close
  "Close a connection to a WiredTiger database."
  [^Connection conn]
  (.close nil conn))

(defn ^Session start-session
  "Start a new session"
  [^Connection conn]
  (.open_session nil conn))

(defn create-table-from-session
  [^Session session table-name table-format]
  (info "Create a new table (if does not exist) named" table-name)
  (.create table-name table-format session))

(defn create-table
  "Create a new table (if does not exist)"
  [^Connection conn table-name table-format]
  (let [session (start-session conn)]
    (create-table-from-session session table-name table-format)))

(defn ^Cursor get-cursor
  [^Session session table-name]
  (.open_cursor table-name nil "overwrite=false" session))

(defn begin-transaction
  "Begin a transaction on a WiredTiger session."
  [^Session session isolation-level]
  (.begin_transaction isolation-level session))

(defn commit-transaction
  "Commit a transaction on a WiredTiger session."
  [^Session session]
  (.commit_transaction nil session))

(defn rollback-transaction
  "Rollback a transaction on a WiredTiger session."
  [^Session session]
  (.rollback_transaction nil session))

;; Error handing
(defmacro with-errors
  "Remaps common errors; takes an operation and returns a :fail or :info op
  when a throw occurs in body."
  [op & body]
  `(try ~@body
     (catch WiredTigerException e#
       (throw e#))))
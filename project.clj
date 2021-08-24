(defproject jepsen.wiredtiger "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [jepsen "0.2.1-SNAPSHOT"]]
  :main jepsen.wiredtiger
  :jvm-opts ["-Djava.awt.headless=true"
             "-Djava.library.path=/usr/local/share/java/wiredtiger-3.3.0"]
  :repl-options {:init-ns jepsen.wiredtiger})

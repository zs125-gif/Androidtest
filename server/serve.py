# -*- coding: utf-8 -*-
"""一个极简的静态文件服务器，替代 Tomcat 提供 JSON 数据。

用法：python serve.py    然后浏览器访问 http://localhost:8080/store_list.json
"""
import os
import http.server
import socketserver

PORT = 8080
ROOT = os.path.dirname(os.path.abspath(__file__))


class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=ROOT, **kwargs)

    def end_headers(self):
        self.send_header("Cache-Control", "no-store")
        self.send_header("Access-Control-Allow-Origin", "*")
        super().end_headers()

    def log_message(self, fmt, *args):
        print("[server] " + fmt % args)


def main():
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.ThreadingTCPServer(("0.0.0.0", PORT), Handler) as httpd:
        print("静态数据服务已启动: http://localhost:%d/" % PORT)
        print("模拟器请访问:      http://10.0.2.2:%d/" % PORT)
        print("按 Ctrl+C 停止")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n已停止")


if __name__ == "__main__":
    main()

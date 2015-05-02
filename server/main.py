
import tornado.ioloop
import tornado.web
import tornado.options
import random
import itertools
import pprint
import hashlib
import string
import tornado.options
import ai
import json
import random

tornado.options.parse_command_line()

x=["flu ","cold ","bronchitis ","asthma "]
y=["10%","15 %","35 %","5 %" ,"70 %","31 %"]

class MainHandler(tornado.web.RequestHandler):
	def post(self):
		survey = self.get_argument('survey',None)
		if not survey:
			return
		survey = [int(x) for x in survey.split(" ")]
		rez = list(fnn.activate(survey))
		maxx = max(rez) + random.uniform(-0.05,0.05)
		idx = rez.index(max(rez))
		self.write("%s %s" %(idx,int(maxx*100)))





application = tornado.web.Application([
	(r"/", MainHandler),
])

if __name__ == "__main__":
	fnn = ai.calc()
	print fnn.activate([0]*41)
	application.listen(8888)
	tornado.ioloop.IOLoop.instance().start()
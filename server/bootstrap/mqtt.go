package bootstrap

import (
	"fmt"
	"shadmin/pkg"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/rs/xid"
)

// NewMQTTClient creates and connects an MQTT client
func NewMQTTClient(env *Env) mqtt.Client {
	broker := fmt.Sprintf("tcp://%s:%s", env.MQTTAddress, env.MQTTTCPPort)
	clientID := fmt.Sprintf("heji-server-%s", xid.New().String())

	opts := mqtt.NewClientOptions().
		AddBroker(broker).
		SetClientID(clientID).
		SetAutoReconnect(true).
		SetMaxReconnectInterval(30 * time.Second).
		SetKeepAlive(60 * time.Second).
		SetCleanSession(false).
		SetOnConnectHandler(func(c mqtt.Client) {
			pkg.Log.Infof("MQTT connected to %s", broker)
		}).
		SetConnectionLostHandler(func(c mqtt.Client, err error) {
			pkg.Log.Warnf("MQTT connection lost: %v", err)
		}).
		SetReconnectingHandler(func(c mqtt.Client, opts *mqtt.ClientOptions) {
			pkg.Log.Info("MQTT reconnecting...")
		})

	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		pkg.Log.Errorf("Failed to connect MQTT: %v", token.Error())
		return client
	}

	return client
}

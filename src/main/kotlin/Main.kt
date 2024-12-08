import aws.sdk.kotlin.runtime.auth.credentials.ProfileCredentialsProvider
import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ec2.model.DescribeAvailabilityZonesRequest
import aws.sdk.kotlin.services.ec2.model.DescribeInstancesRequest
import aws.sdk.kotlin.services.ec2.model.StartInstancesRequest
import java.util.*

suspend fun listEc2Instances(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = DescribeInstancesRequest { }
        val response = ec2Client.describeInstances(request)

        println("EC2 Instances in region $region:")
        response.reservations?.forEach { reservation ->
            reservation.instances?.forEach { instance ->
                println(" - Instance ID: ${instance.instanceId}")
                println("   State: ${instance.state?.name}")
                println("   Type: ${instance.instanceType}")
                println("   Public DNS: ${instance.publicDnsName}")
                println("   Launch Time: ${instance.launchTime}")
                println()
            }
        }
    } catch (e: Exception) {
        println("Error fetching instances: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun listAvailableZones(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = DescribeAvailabilityZonesRequest { }
        val response = ec2Client.describeAvailabilityZones(request)

        println("Available Zones in region $region:")
        response.availabilityZones?.forEach { zone ->
            println(" - Zone Name: ${zone.zoneName}")
            println("   State: ${zone.state}")
            println("   Zone ID: ${zone.zoneId}")
            println("   Messages: ${zone.messages?.joinToString { it.message ?: "" }}")
            println()
        }
    } catch (e: Exception) {
        println("Error fetching availability zones: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun startEc2Instance(region: String, instanceId: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = StartInstancesRequest {
            this.instanceIds = listOf(instanceId)
        }
        val response = ec2Client.startInstances(request)
        println("Starting instance: $instanceId")
        println("Current state: ${response.startingInstances?.firstOrNull()?.currentState?.name}")
    } catch (e: Exception) {
        println("Error starting instance: ${e.message}")
    } finally {
        ec2Client.close()
    }
}


suspend fun main() {
    val region = "ap-northeast-2" // 서울 리전

    val menu = Scanner(System.`in`)
    val idString = Scanner(System.`in`)

    var number = 0;
    while (true) {
        println("--------------------------------------------")
        println("            AWS  EC2 제어 프로그램            ")
        println("--------------------------------------------")
        println("1. 인스턴스 목록 조회")
        println("2. 가용 영역 조회")
        println("3. 인스턴스 시작")
        println("99. 종료")
        println("--------------------------------------------")

        print("입력하세요 : ")

        if(menu.hasNextInt()) {
            number = menu.nextInt();
        } else {
            println("메뉴에 없는 번호입니다.")
            break
        }

        when (number) {
            1 ->  {
                listEc2Instances(region)
            }
            2 -> {
                listAvailableZones(region)
            }
            3 -> {
                print("시작할 인스턴스 ID를 입력하세요: ")
                val instanceId = menu.next()
                startEc2Instance(region, instanceId)
            }
            99 -> return
        }
    }

}
